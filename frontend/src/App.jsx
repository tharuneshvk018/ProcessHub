import React, { useState, useEffect, useCallback } from 'react';
import { Bell, X, ShieldCheck } from 'lucide-react';
import Navbar from './components/Navbar';
import ReporterView from './components/ReporterView';
import SupervisorView from './components/SupervisorView';
import TechnicianView from './components/TechnicianView';
import FinanceView from './components/FinanceView';
import VerificationView from './components/VerificationView';
import AnalyticsView from './components/AnalyticsView';
import IssueDetailModal from './components/IssueDetailModal';
import ReportIssueModal from './components/ReportIssueModal';
import { 
  INITIAL_ISSUES, 
  INITIAL_NOTIFICATIONS, 
  DEMO_DEPARTMENTS, 
  DEMO_CATEGORIES, 
  DEMO_USERS 
} from './services/mockData';
import { api } from './services/api';

export default function App() {
  const [currentRole, setCurrentRole] = useState('ROLE_REPORTER');
  const [currentUser, setCurrentUser] = useState(() => DEMO_USERS['ROLE_REPORTER']);
  const [activeTab, setActiveTab] = useState('reporter');
  const [issues, setIssues] = useState(INITIAL_ISSUES);
  const [notifications, setNotifications] = useState(INITIAL_NOTIFICATIONS);
  const [departments, setDepartments] = useState(DEMO_DEPARTMENTS);
  const [categories, setCategories] = useState(DEMO_CATEGORIES);
  const [selectedIssue, setSelectedIssue] = useState(null);
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);
  const [toast, setToast] = useState(null);

  // Switch user perspective handler
  const handleSelectRole = (newRole) => {
    setCurrentRole(newRole);
    const persona = DEMO_USERS[newRole] || DEMO_USERS.reporter;
    setCurrentUser(persona);

    // Auto-navigate to that role's primary operational workspace
    if (newRole === 'ROLE_REPORTER') setActiveTab('reporter');
    else if (newRole === 'ROLE_SUPERVISOR') setActiveTab('supervisor');
    else if (newRole === 'ROLE_TECHNICIAN') setActiveTab('technician');
    else if (newRole === 'ROLE_FINANCE_APPROVER') setActiveTab('finance');
    else if (newRole === 'ROLE_ADMIN') setActiveTab('analytics');
  };

  // Real-time Toast Notification trigger
  const showToast = (title, message, recipientInfo) => {
    setToast({
      id: Date.now(),
      title,
      message,
      recipientInfo,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    });
  };

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 5000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  // Derive latest active selected issue from issues state to keep modal synchronized
  const activeSelectedIssue = selectedIssue 
    ? (issues.find(i => i.id === selectedIssue.id || i.referenceCode === selectedIssue.referenceCode) || selectedIssue)
    : null;

  // Optional background data sync with backend if online
  const refreshBackendData = useCallback(async () => {
    try {
      const [deptRes, catRes] = await Promise.allSettled([
        api.getDepartments(),
        api.getCategories()
      ]);
      if (deptRes.status === 'fulfilled' && Array.isArray(deptRes.value) && deptRes.value.length > 0) {
        setDepartments(deptRes.value);
      }
      if (catRes.status === 'fulfilled' && Array.isArray(catRes.value) && catRes.value.length > 0) {
        setCategories(catRes.value);
      }
    } catch {
      // Backend is optional/in-memory fallback active
    }
  }, []);

  // Silently authenticate active persona against backend if running
  useEffect(() => {
    const persona = DEMO_USERS[currentRole];
    if (persona && persona.email) {
      api.login(persona.email, 'Password123!')
        .then(() => refreshBackendData())
        .catch(() => {
          // In-memory demo state remains seamlessly active
        });
    }
  }, [currentRole, refreshBackendData]);

  // Atomically append recipient-targeted notifications
  const addNotifications = (newNotifs) => {
    const prepared = newNotifs.map(n => ({
      id: `notif-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`,
      read: false,
      createdAt: new Date().toISOString(),
      ...n
    }));
    setNotifications(prev => [...prepared, ...prev]);
  };

  // ==========================================
  // Collaborative Workflow Engine Handlers
  // ==========================================

  // 1. Problem Reporting (Reporter perspective)
  const handleCreateIssue = async (formData) => {
    const nextNum = 1000 + issues.length + 1;
    const refCode = `PH-${nextNum}`;
    const dept = departments.find(d => d.id === formData.departmentId);
    const cat = categories.find(c => c.id === formData.categoryId);

    const newIssue = {
      id: `iss-${Date.now()}`,
      referenceCode: refCode,
      title: formData.title,
      description: formData.description,
      departmentId: formData.departmentId,
      departmentName: dept?.name || 'Facilities & Maintenance',
      categoryName: cat?.name || 'Plumbing & Water Systems',
      subtype: formData.subtype,
      locationBuilding: formData.locationBuilding,
      locationRoom: formData.locationRoom,
      assetId: formData.assetId,
      priority: formData.priority,
      status: 'UNDER_REVIEW',
      reporterName: currentUser?.fullName || 'Priya Sharma',
      reporterEmail: currentUser?.email || 'priya.sharma@processhub.com',
      assignedTechnicianName: null,
      currentStage: 'UNDER_SUPERVISOR_REVIEW',
      responsibleRole: 'ROLE_SUPERVISOR',
      blockingReason: null,
      nextAction: 'Supervisor (Arjun Kumar) must review report and assign qualified technician',
      slaDueAt: new Date(Date.now() + 1000 * 60 * 60 * 12).toISOString(),
      slaStatus: 'ON_TRACK',
      slaRemainingMinutes: 720,
      createdAt: new Date().toISOString(),
      timeline: [
        {
          id: `tl-${Date.now()}-1`,
          actorName: currentUser?.fullName || 'Priya Sharma',
          action: 'ISSUE_SUBMITTED',
          notes: `Problem report submitted: "${formData.title}" in ${formData.locationBuilding || 'Facilities'} ${formData.locationRoom || ''}`,
          timestamp: new Date().toISOString()
        },
        {
          id: `tl-${Date.now()}-2`,
          actorName: 'Routing Engine',
          action: 'STATUS_CHANGED',
          notes: `Context routing engine assigned issue to ${dept?.name || 'Facilities'} Supervisor Queue`,
          timestamp: new Date().toISOString()
        }
      ],
      expenditures: [],
    };

    // Update shared issues
    setIssues(prev => [newIssue, ...prev]);
    setIsReportModalOpen(false);

    // Notifications:
    addNotifications([
      {
        type: 'SUBMISSION_CONFIRMED',
        title: `Submission Confirmed: ${refCode}`,
        message: `Your report "${formData.title}" has been registered and routed to Facilities Supervisor Arjun Kumar.`,
        referenceCode: refCode,
        recipientRole: 'ROLE_REPORTER',
        recipientEmail: 'priya.sharma@processhub.com'
      },
      {
        type: 'ISSUE_SUBMITTED',
        title: `New Problem Reported: ${refCode}`,
        message: `High priority problem "${formData.title}" reported by Priya Sharma in ${formData.locationBuilding || 'Facilities'}. Triage required.`,
        referenceCode: refCode,
        recipientRole: 'ROLE_SUPERVISOR',
        recipientEmail: 'arjun.kumar@processhub.com'
      }
    ]);

    showToast(
      `New Problem Reported (#${refCode})`,
      `Registered "${formData.title}". Supervisor Arjun Kumar notified for triage.`,
      `Delivered to: Supervisor Arjun Kumar & Reporter Priya Sharma`
    );

    // Background sync to backend if online
    try {
      await api.createIssue(undefined, {
        title: formData.title,
        description: formData.description,
        departmentId: formData.departmentId,
        categoryId: formData.categoryId,
        subtype: formData.subtype,
        locationBuilding: formData.locationBuilding,
        locationRoom: formData.locationRoom,
        assetId: formData.assetId,
        priority: formData.priority,
      });
    } catch {
      // Backend offline, in-memory state updated
    }
  };

  // 2. Supervisor Triage: Start Review
  const handleStartReview = async (issueId) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    const newTimelineEvent = {
      id: `tl-${Date.now()}`,
      actorName: currentUser?.fullName || 'Arjun Kumar',
      action: 'REVIEW_STARTED',
      notes: 'Supervisor initiated diagnostic review, priority validation, and technician matching',
      timestamp: new Date().toISOString()
    };

    const updated = {
      ...target,
      status: 'UNDER_REVIEW',
      currentStage: 'UNDER_SUPERVISOR_REVIEW',
      responsibleRole: 'ROLE_SUPERVISOR',
      slaStatus: 'ON_TRACK',
      nextAction: 'Review diagnostic details and assign qualified technician',
      timeline: [...(target.timeline || []), newTimelineEvent]
    };

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    addNotifications([
      {
        type: 'STATUS_CHANGED',
        title: `Review Underway: ${target.referenceCode}`,
        message: `Supervisor Arjun Kumar has commenced diagnostic review on "${target.title}".`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_REPORTER',
        recipientEmail: 'priya.sharma@processhub.com'
      }
    ]);

    showToast(
      `Supervisor Review Commenced (#${target.referenceCode})`,
      `Arjun Kumar started triage. Reporter Priya Sharma notified.`,
      `Delivered to: Reporter Priya Sharma`
    );

    try {
      await api.startReview(undefined, issueId);
    } catch {
      // Offline fallback
    }
  };

  // 3. Supervisor Assignment (Assign Technician)
  const handleAssignTechnician = async (issueId, technicianId, instructions) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    const techNames = {
      'tech-1': 'Karthik Raj (Senior HVAC & Mechanical)',
      'tech-2': 'Suresh Menon (Master Plumber)',
      'tech-3': 'Vikram Sethi (High-Voltage Electrician)'
    };
    const assignedName = techNames[technicianId] || 'Karthik Raj';

    const newTimelineEvent = {
      id: `tl-${Date.now()}`,
      actorName: currentUser?.fullName || 'Arjun Kumar',
      action: 'TECHNICIAN_ASSIGNED',
      notes: `Assigned work order to ${assignedName}. Instructions: "${instructions || 'Inspect site, confirm diagnosis and execute remediation'}"`,
      timestamp: new Date().toISOString()
    };

    const updated = {
      ...target,
      status: 'ASSIGNED',
      assignedTechnicianName: assignedName,
      assignedTechnicianId: technicianId,
      currentStage: 'ASSIGNED_WORK_ORDER',
      responsibleRole: 'ROLE_TECHNICIAN',
      blockingReason: null,
      slaStatus: 'ON_TRACK',
      nextAction: `Technician (${assignedName}) must inspect site, confirm diagnosis and initiate repair`,
      timeline: [...(target.timeline || []), newTimelineEvent]
    };

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    // Notifications:
    addNotifications([
      {
        type: 'ASSIGNMENT',
        title: `Work Order Assigned: ${target.referenceCode}`,
        message: `Assigned to you by Arjun Kumar: "${target.title}". Instructions: "${instructions || 'Inspect and execute remediation'}"`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_TECHNICIAN',
        recipientEmail: 'karthik.raj@processhub.com'
      },
      {
        type: 'ASSIGNMENT',
        title: `Technician Assigned: ${target.referenceCode}`,
        message: `Karthik Raj has been assigned to your report "${target.title}". Work order dispatched.`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_REPORTER',
        recipientEmail: 'priya.sharma@processhub.com'
      }
    ]);

    showToast(
      `Technician Assigned (#${target.referenceCode})`,
      `Work order assigned to Karthik Raj. Technician and Reporter notified in real time.`,
      `Delivered to: Technician Karthik Raj & Reporter Priya Sharma`
    );

    try {
      await api.assignTechnician(undefined, issueId, technicianId, instructions);
    } catch {
      // Offline fallback
    }
  };

  // 4. Supervisor Rejection
  const handleRejectIssue = async (issueId, reason) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    const newTimelineEvent = {
      id: `tl-${Date.now()}`,
      actorName: currentUser?.fullName || 'Arjun Kumar',
      action: 'ISSUE_REJECTED',
      notes: `Supervisor rejected report. Mandatory rationale: "${reason}"`,
      timestamp: new Date().toISOString()
    };

    const updated = {
      ...target,
      status: 'REJECTED',
      currentStage: 'REJECTED',
      responsibleRole: 'NONE',
      blockingReason: `Rejection rationale: ${reason}`,
      slaStatus: 'COMPLETED',
      nextAction: 'Ticket closed as rejected. Reporter may submit clarified request if applicable.',
      rejectionReason: reason,
      timeline: [...(target.timeline || []), newTimelineEvent]
    };

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    addNotifications([
      {
        type: 'STATUS_CHANGED',
        title: `Problem Report Declined: ${target.referenceCode}`,
        message: `Report "${target.title}" was reviewed and declined by Arjun Kumar. Reason: "${reason}"`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_REPORTER',
        recipientEmail: 'priya.sharma@processhub.com'
      }
    ]);

    showToast(
      `Problem Report Declined (#${target.referenceCode})`,
      `Report declined by Arjun Kumar. Rationale logged to timeline and dispatched to reporter.`,
      `Delivered to: Reporter Priya Sharma`
    );

    try {
      await api.rejectIssue(undefined, issueId, reason);
    } catch {
      // Offline fallback
    }
  };

  // 5. Supervisor Request Information (RFI)
  const handleRequestInfo = async (issueId, questions) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    const newTimelineEvent = {
      id: `tl-${Date.now()}`,
      actorName: currentUser?.fullName || 'Arjun Kumar',
      action: 'INFO_REQUESTED',
      notes: `Supervisor requested clarification: "${questions}"`,
      timestamp: new Date().toISOString()
    };

    const updated = {
      ...target,
      status: 'MORE_INFO_REQUESTED',
      currentStage: 'AWAITING_REPORTER_INPUT',
      responsibleRole: 'ROLE_REPORTER',
      slaStatus: 'AT_RISK',
      blockingReason: 'Awaiting clarification response from reporter before work can be assigned',
      nextAction: `Reporter (${target.reporterName}) must provide requested details: "${questions}"`,
      timeline: [...(target.timeline || []), newTimelineEvent]
    };

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    addNotifications([
      {
        type: 'INFO_REQUESTED',
        title: `Additional Information Requested: ${target.referenceCode}`,
        message: `Arjun Kumar requested details on "${target.title}": "${questions}"`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_REPORTER',
        recipientEmail: 'priya.sharma@processhub.com'
      }
    ]);

    showToast(
      `Information Requested (#${target.referenceCode})`,
      `RFI dispatched to Reporter Priya Sharma: "${questions}"`,
      `Delivered to: Reporter Priya Sharma`
    );

    try {
      await api.requestInfo(undefined, issueId, questions);
    } catch {
      // Offline fallback
    }
  };

  // 6. Reporter Responds to Information Request
  const handleProvideInfo = async (issueId, responseText) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    const newTimelineEvent = {
      id: `tl-${Date.now()}`,
      actorName: currentUser?.fullName || 'Priya Sharma',
      action: 'INFO_PROVIDED',
      notes: `Reporter submitted clarification: "${responseText}"`,
      timestamp: new Date().toISOString()
    };

    const updated = {
      ...target,
      status: 'UNDER_REVIEW',
      currentStage: 'UNDER_SUPERVISOR_REVIEW',
      responsibleRole: 'ROLE_SUPERVISOR',
      blockingReason: null,
      slaStatus: 'ON_TRACK',
      nextAction: 'Supervisor (Arjun Kumar) must review reporter clarifications and resume triage',
      timeline: [...(target.timeline || []), newTimelineEvent]
    };

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    addNotifications([
      {
        type: 'INFO_PROVIDED',
        title: `Clarification Provided: ${target.referenceCode}`,
        message: `Priya Sharma answered clarification request on "${target.title}": "${responseText}"`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_SUPERVISOR',
        recipientEmail: 'arjun.kumar@processhub.com'
      }
    ]);

    showToast(
      `Clarification Submitted (#${target.referenceCode})`,
      `Priya Sharma answered inquiry. Supervisor Arjun Kumar notified to resume triage.`,
      `Delivered to: Supervisor Arjun Kumar`
    );

    try {
      await api.provideInfo(undefined, issueId, responseText);
    } catch {
      // Offline fallback
    }
  };

  // 7. Technician: Start Work
  const handleStartWork = async (issueId) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    const newTimelineEvent = {
      id: `tl-${Date.now()}`,
      actorName: currentUser?.fullName || 'Karthik Raj',
      action: 'WORK_STARTED',
      notes: 'Technician arrived on-site, completed initial safety isolation, and initiated repair procedures',
      timestamp: new Date().toISOString()
    };

    const updated = {
      ...target,
      status: 'IN_PROGRESS',
      currentStage: 'REMEDIATION_IN_PROGRESS',
      responsibleRole: 'ROLE_TECHNICIAN',
      blockingReason: null,
      slaStatus: 'ON_TRACK',
      nextAction: `Technician (${target.assignedTechnicianName || 'Karthik Raj'}) is executing on-site diagnostic and repair tasks`,
      timeline: [...(target.timeline || []), newTimelineEvent]
    };

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    addNotifications([
      {
        type: 'STATUS_CHANGED',
        title: `Repair Work Started: ${target.referenceCode}`,
        message: `Karthik Raj has commenced physical repair on "${target.title}".`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_SUPERVISOR',
        recipientEmail: 'arjun.kumar@processhub.com'
      },
      {
        type: 'STATUS_CHANGED',
        title: `Technician On-Site: ${target.referenceCode}`,
        message: `Work is now actively underway on your problem report "${target.title}".`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_REPORTER',
        recipientEmail: 'priya.sharma@processhub.com'
      }
    ]);

    showToast(
      `Repair Work Started (#${target.referenceCode})`,
      `Karthik Raj commenced repair. Supervisor and Reporter notified in real time.`,
      `Delivered to: Supervisor Arjun Kumar & Reporter Priya Sharma`
    );

    try {
      await api.startWork(undefined, issueId);
    } catch {
      // Offline fallback
    }
  };

  // 8. Technician: Update Progress
  const handleUpdateProgress = async (issueId, progressPct, notes) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    const newTimelineEvent = {
      id: `tl-${Date.now()}`,
      actorName: currentUser?.fullName || 'Karthik Raj',
      action: 'PROGRESS_UPDATED',
      notes: `Progress logged at ${progressPct}%. Operational notes: "${notes}"`,
      timestamp: new Date().toISOString()
    };

    const updated = {
      ...target,
      progressPct,
      slaStatus: 'ON_TRACK',
      timeline: [...(target.timeline || []), newTimelineEvent]
    };

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    addNotifications([
      {
        type: 'STATUS_CHANGED',
        title: `Progress Update: ${target.referenceCode} (${progressPct}%)`,
        message: `Karthik Raj updated repair progress to ${progressPct}% on "${target.title}": "${notes}"`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_SUPERVISOR',
        recipientEmail: 'arjun.kumar@processhub.com'
      },
      {
        type: 'STATUS_CHANGED',
        title: `Repair Update: ${target.referenceCode} (${progressPct}%)`,
        message: `Technician logged ${progressPct}% completion on "${target.title}".`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_REPORTER',
        recipientEmail: 'priya.sharma@processhub.com'
      }
    ]);

    showToast(
      `Progress Logged (${progressPct}%) (#${target.referenceCode})`,
      `Logged progress update. Supervisor and Reporter notified in real time.`,
      `Delivered to: Supervisor Arjun Kumar & Reporter Priya Sharma`
    );

    try {
      await api.updateProgress(undefined, issueId, progressPct, notes);
    } catch {
      // Offline fallback
    }
  };

  // 9. Technician: Request Expenditure (Conditional Finance Gate: $100 Threshold)
  const handleRequestExpenditure = async (issueId, amount, justification) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    const numAmount = parseFloat(amount);
    const requiresFinance = numAmount > 100.00;

    const newExp = {
      id: `exp-${Date.now()}`,
      amount: numAmount,
      justification,
      status: requiresFinance ? 'PENDING' : 'APPROVED',
      requestedByName: currentUser?.fullName || 'Karthik Raj',
      createdAt: new Date().toISOString()
    };

    let updated;
    if (requiresFinance) {
      const newTimelineEvent = {
        id: `tl-${Date.now()}`,
        actorName: currentUser?.fullName || 'Karthik Raj',
        action: 'EXPENDITURE_REQUESTED',
        notes: `Technician requested $${numAmount.toFixed(2)} for: "${justification}". Exceeds $100.00 threshold; gated for Finance Approver authorization.`,
        timestamp: new Date().toISOString()
      };

      updated = {
        ...target,
        status: 'AWAITING_FINANCE',
        currentStage: 'AWAITING_FINANCE_APPROVAL',
        responsibleRole: 'ROLE_FINANCE_APPROVER',
        slaStatus: 'AT_RISK',
        blockingReason: `Expenditure authorization requested by Technician ($${numAmount.toFixed(2)}) exceeds auto-approval threshold ($100.00)`,
        nextAction: 'Finance Approver (Divya Nair) must review expenditure quote and approve or decline with notes',
        expenditures: [...(target.expenditures || []), newExp],
        timeline: [...(target.timeline || []), newTimelineEvent]
      };

      addNotifications([
        {
          type: 'EXPENDITURE_REQUESTED',
          title: `Spend Authorization Required: ${target.referenceCode}`,
          message: `Karthik Raj requested $${numAmount.toFixed(2)} for "${target.title}". Justification: "${justification}". Financial sign-off required.`,
          referenceCode: target.referenceCode,
          recipientRole: 'ROLE_FINANCE_APPROVER',
          recipientEmail: 'divya.nair@processhub.com'
        }
      ]);

      showToast(
        `Spend Authorization Requested ($${numAmount.toFixed(2)})`,
        `Exceeds $100 limit. Routed to Finance Approver Divya Nair for approval.`,
        `Delivered to: Finance Approver Divya Nair`
      );
    } else {
      const newTimelineEvent = {
        id: `tl-${Date.now()}`,
        actorName: 'Finance Rules Engine',
        action: 'EXPENDITURE_AUTO_APPROVED',
        notes: `Spend request of $${numAmount.toFixed(2)} auto-approved (within $100.00 delegated threshold).`,
        timestamp: new Date().toISOString()
      };

      updated = {
        ...target,
        expenditures: [...(target.expenditures || []), newExp],
        timeline: [...(target.timeline || []), newTimelineEvent]
      };

      showToast(
        `Spend Auto-Approved ($${numAmount.toFixed(2)})`,
        `Spend request is &le; $100.00 threshold. Work proceeds without delay.`,
        `Auto-approved under delegated authority`
      );
    }

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    try {
      await api.requestExpenditure(undefined, issueId, amount, justification);
    } catch {
      // Offline fallback
    }
  };

  // 10. Finance Approver: Decide Expenditure (Approve or Reject)
  const handleDecideExpenditure = async (issueId, expenditureId, approved, notes) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    let expAmount = 380;
    const updatedExps = (target.expenditures || []).map(e => {
      if (e.id !== expenditureId && String(e.id) !== String(expenditureId)) return e;
      expAmount = e.amount;
      return {
        ...e,
        status: approved ? 'APPROVED' : 'REJECTED',
        approved,
        decisionNotes: notes,
        decidedAt: new Date().toISOString(),
        decidedBy: currentUser?.fullName || 'Divya Nair'
      };
    });

    const newTimelineEvent = {
      id: `tl-${Date.now()}`,
      actorName: currentUser?.fullName || 'Divya Nair',
      action: approved ? 'EXPENDITURE_APPROVED' : 'EXPENDITURE_REJECTED',
      notes: `Finance Approver ${approved ? 'APPROVED' : 'REJECTED'} expenditure of $${expAmount}. Notes: "${notes}"`,
      timestamp: new Date().toISOString()
    };

    const newStatus = approved ? 'IN_PROGRESS' : 'UNDER_REVIEW';
    const newStage = approved ? 'REMEDIATION_IN_PROGRESS' : 'EXPENDITURE_REJECTED_TRIAGE';
    const newRole = approved ? 'ROLE_TECHNICIAN' : 'ROLE_SUPERVISOR';
    const newBlocker = approved ? null : `Expenditure rejected by Finance: ${notes}`;
    const newNextAction = approved 
      ? 'Technician (Karthik Raj) authorized to procure parts and execute repair'
      : 'Supervisor (Arjun Kumar) must review alternative remediation plan or cancel work order';

    const updated = {
      ...target,
      status: newStatus,
      currentStage: newStage,
      responsibleRole: newRole,
      blockingReason: newBlocker,
      nextAction: newNextAction,
      slaStatus: approved ? 'ON_TRACK' : 'AT_RISK',
      expenditures: updatedExps,
      timeline: [...(target.timeline || []), newTimelineEvent]
    };

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    addNotifications([
      {
        type: approved ? 'EXPENDITURE_APPROVED' : 'EXPENDITURE_REJECTED',
        title: `Expenditure ${approved ? 'Approved' : 'Rejected'}: ${target.referenceCode}`,
        message: `Your spend request of $${expAmount} was ${approved ? 'APPROVED' : 'DECLINED'} by Divya Nair. Notes: "${notes}"`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_TECHNICIAN',
        recipientEmail: 'karthik.raj@processhub.com'
      },
      {
        type: approved ? 'EXPENDITURE_APPROVED' : 'EXPENDITURE_REJECTED',
        title: `Finance Decision Logged: ${target.referenceCode}`,
        message: `Divya Nair ${approved ? 'APPROVED' : 'DECLINED'} expenditure for "${target.title}". Work state updated.`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_SUPERVISOR',
        recipientEmail: 'arjun.kumar@processhub.com'
      }
    ]);

    showToast(
      `Finance Decision: ${approved ? 'Approved' : 'Declined'} ($${expAmount})`,
      `Divya Nair logged finance decision. Technician Karthik Raj notified in real time.`,
      `Delivered to: Technician Karthik Raj & Supervisor Arjun Kumar`
    );

    try {
      await api.decideExpenditure(undefined, issueId, expenditureId, approved, notes);
    } catch {
      // Offline fallback
    }
  };

  // 11. Technician: Complete Work & Submit Resolution
  const handleSubmitResolution = async (issueId, resData) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    const newTimelineEvent = {
      id: `tl-${Date.now()}`,
      actorName: currentUser?.fullName || 'Karthik Raj',
      action: 'RESOLUTION_SUBMITTED',
      notes: `Technician completed physical repair. Summary: "${resData.workSummary}". Root cause: "${resData.rootCause}". Labor: ${resData.laborHours || 2}h. Awaiting independent verification sign-off.`,
      timestamp: new Date().toISOString()
    };

    const updated = {
      ...target,
      status: 'RESOLVED_PENDING_VERIFICATION',
      currentStage: 'RESOLVED_PENDING_VERIFICATION',
      responsibleRole: 'ROLE_SUPERVISOR',
      blockingReason: 'Pending independent verification sign-off (Technician cannot verify own work)',
      slaStatus: 'ON_TRACK',
      nextAction: 'Authorized supervisor or reporter must independently verify work outcome',
      workSummary: resData.workSummary,
      rootCause: resData.rootCause,
      laborHours: parseFloat(resData.laborHours || 0),
      partsUsed: resData.partsUsed,
      resolutionEvidenceUrl: resData.evidenceUrl || 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&q=80&w=800',
      resolvedAt: new Date().toISOString(),
      timeline: [...(target.timeline || []), newTimelineEvent]
    };

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    addNotifications([
      {
        type: 'RESOLUTION_SUBMITTED',
        title: `Ready for Verification: ${target.referenceCode}`,
        message: `Karthik Raj completed repair on "${target.title}". Independent verification sign-off required.`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_SUPERVISOR',
        recipientEmail: 'arjun.kumar@processhub.com'
      },
      {
        type: 'RESOLUTION_SUBMITTED',
        title: `Repair Completed: ${target.referenceCode}`,
        message: `Work on "${target.title}" has been completed and is awaiting supervisory verification.`,
        referenceCode: target.referenceCode,
        recipientRole: 'ROLE_REPORTER',
        recipientEmail: 'priya.sharma@processhub.com'
      }
    ]);

    showToast(
      `Repair Completed (#${target.referenceCode})`,
      `Resolution submitted by Karthik Raj. Supervisor Arjun Kumar notified for verification.`,
      `Delivered to: Supervisor Arjun Kumar & Reporter Priya Sharma`
    );

    try {
      await api.submitResolution(undefined, issueId, resData);
    } catch {
      // Offline fallback
    }
  };

  // 12. Verification: Independent Sign-Off or Reopen Rework
  const handleVerifyResolution = async (issueId, approved, verificationNotes) => {
    const target = issues.find(i => i.id === issueId || i.referenceCode === issueId);
    if (!target) return;

    const verifierName = currentUser?.fullName || 'Arjun Kumar';

    let updated;
    if (approved) {
      const event1 = {
        id: `tl-${Date.now()}-1`,
        actorName: verifierName,
        action: 'RESOLUTION_VERIFIED',
        notes: `Independent verification sign-off APPROVED by ${verifierName}. Notes: "${verificationNotes}"`,
        timestamp: new Date().toISOString()
      };
      const event2 = {
        id: `tl-${Date.now()}-2`,
        actorName: 'Audit System',
        action: 'ISSUE_CLOSED',
        notes: 'Ticket closed in accordance with enterprise verification governance',
        timestamp: new Date().toISOString()
      };

      updated = {
        ...target,
        status: 'CLOSED',
        currentStage: 'VERIFIED_AND_CLOSED',
        responsibleRole: 'NONE',
        blockingReason: null,
        slaStatus: 'COMPLETED',
        nextAction: 'Issue closed successfully with complete audit trail',
        verifiedBy: verifierName,
        verifiedAt: new Date().toISOString(),
        verificationNotes,
        timeline: [...(target.timeline || []), event1, event2]
      };

      addNotifications([
        {
          type: 'ISSUE_CLOSED',
          title: `Issue Verified & Closed: ${target.referenceCode}`,
          message: `Repair on "${target.title}" has been verified and permanently closed. Full audit timeline archived.`,
          referenceCode: target.referenceCode,
          recipientRole: 'ROLE_REPORTER',
          recipientEmail: 'priya.sharma@processhub.com'
        },
        {
          type: 'RESOLUTION_VERIFIED',
          title: `Work Verified: ${target.referenceCode}`,
          message: `Your resolution on "${target.title}" was verified and signed off by ${verifierName}.`,
          referenceCode: target.referenceCode,
          recipientRole: 'ROLE_TECHNICIAN',
          recipientEmail: 'karthik.raj@processhub.com'
        }
      ]);

      showToast(
        `Issue Verified & Closed (#${target.referenceCode})`,
        `Independent sign-off approved by ${verifierName}. Issue permanently closed.`,
        `Delivered to: Reporter Priya Sharma & Technician Karthik Raj`
      );
    } else {
      const event1 = {
        id: `tl-${Date.now()}`,
        actorName: verifierName,
        action: 'VERIFICATION_FAILED',
        notes: `Verification REJECTED by ${verifierName}. Rework required. Notes: "${verificationNotes}"`,
        timestamp: new Date().toISOString()
      };

      updated = {
        ...target,
        status: 'IN_PROGRESS',
        currentStage: 'REMEDIATION_IN_PROGRESS',
        responsibleRole: 'ROLE_TECHNICIAN',
        blockingReason: `Verification failed rework order: "${verificationNotes}"`,
        slaStatus: 'AT_RISK',
        nextAction: `Technician (${target.assignedTechnicianName || 'Karthik Raj'}) must address rework deficiency noted in verification`,
        timeline: [...(target.timeline || []), event1]
      };

      addNotifications([
        {
          type: 'VERIFICATION_FAILED',
          title: `Rework Required: ${target.referenceCode}`,
          message: `Verification sign-off was declined by ${verifierName}. Rework notes: "${verificationNotes}"`,
          referenceCode: target.referenceCode,
          recipientRole: 'ROLE_TECHNICIAN',
          recipientEmail: 'karthik.raj@processhub.com'
        },
        {
          type: 'STATUS_CHANGED',
          title: `Rework Ordered: ${target.referenceCode}`,
          message: `Supervisor requested additional remediation on "${target.title}".`,
          referenceCode: target.referenceCode,
          recipientRole: 'ROLE_REPORTER',
          recipientEmail: 'priya.sharma@processhub.com'
        }
      ]);

      showToast(
        `Verification Declined — Rework Ordered (#${target.referenceCode})`,
        `Rework order dispatched to Technician Karthik Raj: "${verificationNotes}"`,
        `Delivered to: Technician Karthik Raj & Reporter Priya Sharma`
      );
    }

    setIssues(prev => prev.map(i => (i.id === target.id ? updated : i)));
    if (selectedIssue?.id === target.id) setSelectedIssue(updated);

    try {
      await api.verifyResolution(undefined, issueId, approved, verificationNotes);
    } catch {
      // Offline fallback
    }
  };

  // 13. Notifications Read/Unread Actions
  const handleMarkNotificationRead = (id) => {
    setNotifications(prev => prev.map(n => (n.id === id ? { ...n, read: true } : n)));
  };

  const handleMarkAllRead = (idsToMark) => {
    if (Array.isArray(idsToMark)) {
      const idSet = new Set(idsToMark);
      setNotifications(prev => prev.map(n => (idSet.has(n.id) ? { ...n, read: true } : n)));
    } else {
      setNotifications(prev => prev.map(n => ({ ...n, read: true })));
    }
  };

  // Click on notification to jump directly to issue detail modal
  const handleSelectNotification = (notif) => {
    const match = issues.find(i => i.referenceCode === notif.referenceCode || i.id === notif.issueId);
    if (match) {
      setSelectedIssue(match);
    }
  };

  // 14. SLA Escalation Trigger (Analytics / Admin)
  const handleTriggerSlaEscalation = async () => {
    const escalatedIssues = issues.filter(i => i.referenceCode === 'PH-1002' || i.status === 'AWAITING_FINANCE');
    const escalatedCount = escalatedIssues.length || 1;
    setIssues(prev => prev.map(i => {
      if (i.referenceCode === 'PH-1002' || i.status === 'AWAITING_FINANCE') {
        return {
          ...i,
          slaBreached: true,
          slaStatus: 'BREACHED',
          slaRemainingMinutes: -45,
          timeline: [
            ...i.timeline,
            {
              id: `tl-${Date.now()}`,
              actorName: 'SLA Escalation Service',
              action: 'SLA_BREACHED',
              notes: 'SLA deadline exceeded. High-priority escalation dispatched to department supervisor and admin.',
              timestamp: new Date().toISOString()
            }
          ]
        };
      }
      return i;
    }));

    addNotifications([
      {
        type: 'SLA_BREACH_WARNING',
        title: 'SLA Breach Escalation Alert: PH-1002',
        message: 'Critical pump maintenance #PH-1002 exceeded 4-hour threshold window.',
        referenceCode: 'PH-1002',
        recipientRole: 'ROLE_ADMIN',
        recipientEmail: 'rahul.verma@processhub.com'
      },
      {
        type: 'SLA_BREACH_WARNING',
        title: 'SLA Escalation: PH-1002',
        message: 'Critical pump maintenance #PH-1002 exceeded response window. Expedite review.',
        referenceCode: 'PH-1002',
        recipientRole: 'ROLE_SUPERVISOR',
        recipientEmail: 'arjun.kumar@processhub.com'
      }
    ]);

    showToast(
      'SLA Escalation Triggered',
      `Evaluated all active issues. Critical item #PH-1002 escalated for overdue SLA window.`,
      `Delivered to: Administrator Rahul Verma & Supervisor Arjun Kumar`
    );

    return { status: 'SCANNED', escalatedCount };
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Real-Time Toast Notification Popup */}
      {toast && (
        <div style={{
          position: 'fixed',
          top: '20px',
          right: '24px',
          zIndex: 9999,
          background: 'var(--bg-primary)',
          border: '1px solid var(--border-medium)',
          borderLeft: '4px solid var(--indigo-500)',
          borderRadius: 'var(--radius-lg)',
          boxShadow: 'var(--shadow-xl)',
          padding: '14px 18px',
          display: 'flex',
          alignItems: 'flex-start',
          gap: '12px',
          maxWidth: '440px',
          backdropFilter: 'blur(12px)',
          animation: 'slideInRight 0.25s ease-out'
        }}>
          <div style={{
            width: '32px',
            height: '32px',
            borderRadius: 'var(--radius-full)',
            background: 'rgba(99, 102, 241, 0.15)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--indigo-400)',
            flexShrink: 0,
            marginTop: '2px'
          }}>
            <Bell size={16} />
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontWeight: 700, fontSize: '0.86rem', color: 'var(--text-primary)' }}>
                {toast.title}
              </span>
              <button 
                onClick={() => setToast(null)}
                style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', padding: '0 2px' }}
              >
                <X size={15} />
              </button>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '4px 0 6px', lineHeight: 1.4 }}>
              {toast.message}
            </p>
            {toast.recipientInfo && (
              <div style={{ 
                fontSize: '0.72rem', 
                color: 'var(--indigo-400)', 
                fontWeight: 600, 
                display: 'flex', 
                alignItems: 'center', 
                gap: '4px',
                background: 'rgba(99, 102, 241, 0.08)',
                padding: '3px 8px',
                borderRadius: '4px',
                width: 'fit-content'
              }}>
                <ShieldCheck size={12} />
                <span>{toast.recipientInfo}</span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Header with User Perspective Switcher & Recipient Notifications */}
      <Navbar 
        currentRole={currentRole}
        currentUser={currentUser}
        onSelectRole={handleSelectRole}
        notifications={notifications}
        onMarkNotificationRead={handleMarkNotificationRead}
        onMarkAllRead={handleMarkAllRead}
        onSelectNotification={handleSelectNotification}
        onOpenReportModal={() => setIsReportModalOpen(true)}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />

      {/* Main Content Workspace */}
      <main className="app-container" style={{ flex: 1 }}>
        {activeTab === 'reporter' && (
          <ReporterView 
            issues={issues}
            onSelectIssue={setSelectedIssue}
            onOpenReportModal={() => setIsReportModalOpen(true)}
          />
        )}

        {activeTab === 'supervisor' && (
          <SupervisorView 
            issues={issues}
            departments={departments}
            onSelectIssue={setSelectedIssue}
            onStartReview={handleStartReview}
            onAssignTechnician={handleAssignTechnician}
            onRejectIssue={handleRejectIssue}
            onRequestInfo={handleRequestInfo}
            onEvaluateRouting={(id) => console.log('Routing evaluated', id)}
          />
        )}

        {activeTab === 'technician' && (
          <TechnicianView 
            issues={issues}
            onSelectIssue={setSelectedIssue}
            onStartWork={handleStartWork}
            onUpdateProgress={handleUpdateProgress}
            onRequestExpenditure={handleRequestExpenditure}
            onSubmitResolution={handleSubmitResolution}
          />
        )}

        {activeTab === 'finance' && (
          <FinanceView 
            issues={issues}
            onDecideExpenditure={handleDecideExpenditure}
            onSelectIssue={setSelectedIssue}
          />
        )}

        {activeTab === 'verification' && (
          <VerificationView 
            issues={issues}
            currentRole={currentRole}
            currentUser={currentUser}
            onVerifyResolution={handleVerifyResolution}
            onSelectIssue={setSelectedIssue}
          />
        )}

        {activeTab === 'analytics' && (
          <AnalyticsView 
            issues={issues}
            currentUser={currentUser}
            onTriggerSlaEscalation={handleTriggerSlaEscalation}
          />
        )}
      </main>

      {/* Report Modal */}
      <ReportIssueModal 
        isOpen={isReportModalOpen}
        onClose={() => setIsReportModalOpen(false)}
        departments={departments}
        categories={categories}
        onSubmit={handleCreateIssue}
      />

      {/* Deep Inspection Drawer / Modal */}
      <IssueDetailModal 
        issue={activeSelectedIssue}
        onClose={() => setSelectedIssue(null)}
        onProvideInfo={handleProvideInfo}
      />
    </div>
  );
}
