package com.processhub.service;

import com.processhub.domain.entity.Issue;
import com.processhub.domain.entity.IssueAttachment;
import com.processhub.domain.entity.IssueTimelineEvent;
import com.processhub.domain.entity.User;
import com.processhub.domain.enums.TimelineAction;
import com.processhub.domain.repository.IssueAttachmentRepository;
import com.processhub.domain.repository.IssueRepository;
import com.processhub.domain.repository.IssueTimelineEventRepository;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.AttachmentDownloadResponse;
import com.processhub.dto.AttachmentResponse;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttachmentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L; // 10MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif", "pdf", "txt", "doc", "docx"
    );

    private static final Set<String> PROHIBITED_EXTENSIONS = Set.of(
            "exe", "bat", "sh", "cmd", "js", "vbs", "msi", "php", "py", "dll", "scr", "jar", "com"
    );

    private final IssueAttachmentRepository attachmentRepository;
    private final IssueRepository issueRepository;
    private final IssueTimelineEventRepository timelineRepository;
    private final UserRepository userRepository;
    private final OrganizationService organizationService;

    public AttachmentService(IssueAttachmentRepository attachmentRepository,
                             IssueRepository issueRepository,
                             IssueTimelineEventRepository timelineRepository,
                             UserRepository userRepository,
                             OrganizationService organizationService) {
        this.attachmentRepository = attachmentRepository;
        this.issueRepository = issueRepository;
        this.timelineRepository = timelineRepository;
        this.userRepository = userRepository;
        this.organizationService = organizationService;
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 10MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name must not be empty");
        }

        String extension = getFileExtension(originalFilename);
        if (extension.isEmpty() || PROHIBITED_EXTENSIONS.contains(extension) || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "File extension '" + extension + "' is not permitted. Only standard images (JPEG, PNG, WEBP, GIF) and documents (PDF, DOCX, TXT) are accepted.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "Content-Type '" + contentType + "' is not permitted. Allowed types: JPEG, PNG, WEBP, GIF, PDF, DOCX, TXT.");
        }
    }

    @Transactional
    public AttachmentResponse storeAttachment(UUID orgId, UUID issueId, MultipartFile file, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);
        validateFile(file);

        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file data", e);
        }

        IssueAttachment attachment = new IssueAttachment(
                UUID.randomUUID(),
                issue,
                sanitizedFilename,
                file.getContentType(),
                file.getSize(),
                null,
                data,
                currentUser
        );

        IssueAttachment saved = attachmentRepository.save(attachment);

        IssueTimelineEvent event = new IssueTimelineEvent(
                UUID.randomUUID(),
                issue,
                currentUser,
                TimelineAction.ATTACHMENT_ADDED.name(),
                issue.getStatus().name(),
                issue.getStatus().name(),
                "Attachment added: " + sanitizedFilename + " (" + formatFileSize(file.getSize()) + ")"
        );
        timelineRepository.save(event);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsForIssue(UUID orgId, UUID issueId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        return attachmentRepository.findByIssueIdOrderByCreatedAtAsc(issueId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttachmentDownloadResponse getAttachmentData(UUID orgId, UUID issueId, UUID attachmentId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        IssueAttachment attachment = attachmentRepository.findByIdAndIssueId(attachmentId, issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with ID: " + attachmentId));

        return new AttachmentDownloadResponse(
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getFileData()
        );
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT).trim();
    }

    private String sanitizeFilename(String filename) {
        String baseName = Paths.get(filename).getFileName().toString();
        return baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int z = (63 - Long.numberOfLeadingZeros(bytes)) / 10;
        return String.format(Locale.ROOT, "%.1f %sB", (double) bytes / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    public AttachmentResponse mapToResponse(IssueAttachment att) {
        String downloadUrl = "/api/v1/organizations/" + att.getIssue().getOrganization().getId()
                + "/issues/" + att.getIssue().getId()
                + "/attachments/" + att.getId();

        return new AttachmentResponse(
                att.getId(),
                att.getIssue().getId(),
                att.getFileName(),
                att.getContentType(),
                att.getFileSize(),
                att.getUploadedBy().getId(),
                att.getUploadedBy().getFullName(),
                downloadUrl,
                att.getCreatedAt()
        );
    }
}
