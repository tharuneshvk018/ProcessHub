import React, { useState, useEffect } from 'react';
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
import api from './services/api';

export default function App() {
  const [currentRole, setCurrentRole] = useState('ROLE_REPORTER');
  const [activeTab, setActiveTab] = useState('reporter');
  const [issues, setIssues] = useState(INITIAL_ISSUES);
  const [notifications, setNotifications] = useState(INITIAL_NOTIFICATIONS);
  const [departments] = useState(DEMO_DEPARTMENTS);
  const [categories] = useState(DEMO_CATEGORIES);

  const [selectedIssue, setSelectedIssue] = useState(null);
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);

  // Automatically switch tab when role is toggled for smooth demo flow
  useEffect(() => {
    if (currentRole === 'ROLE_REPORTER') setActiveTab('reporter');
    else if (currentRole === 'ROLE_SUPERVISOR') setActiveTab('supervisor');
    else if (currentRole === 'ROLE_TECHNICIAN') setActiveTab('technician');
    else if (currentRole === 'ROLE_FINANCE_APPROVER') setActiveTab('finance');
    else if (currentRole === 'ROLE_ADMIN') setActiveTab('analytics');
  }, [currentRole]);

  // Sync selected issue details when state updates
  useEffect(() => {
    if (selectedIssue) {
      const refreshed = issues.find(i => i.id === selectedIssue.id);
      if (refreshed) setSelectedIssue(refreshed);
    }
  }, [issues]);

  // ==========================================
  // Workflow Engine Handlers
  // ==========================================

  // 1. Problem Reporting (Atomic Reference + Routing)
  const handleCreateIssue = (formData) => {
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
      categoryName: cat?.name || 'Plumbing',
      subtype: formData.subtype,
      locationBuilding: formData.locationBuilding,
      locationRoom: formData.locationRoom,
      assetId: formData.assetId,
      priority: formData.priority,
      status: 'UNDER_REVIEW',
      reporterName: 'Alice Reporter',
      reporterEmail: 'alice.reporter@processhub.com',
      assignedTechnicianName: null,
      currentStage: 'UNDER_SUPERVISOR_REVIEW',
      responsibleRole: 'ROLE_SUPERVISOR',
      blockingReason: null,
      nextAction: `Supervisor (${dept?.name || 'Department'}) must review report and assign eligible technician`,
      slaDueAt: new Date(Date.now() + 1000 * 60 * 60 * (formData.priority === 'CRITICAL' ? 4 : formData.priority === 'HIGH' ? 12 : 48)).toISOString(),
      slaBreached: false,
      slaStatus: 'ON_TRACK',
      slaRemainingMinutes: formData.priority === 'CRITICAL' ? 240 : 720,
      createdAt: new Date().toISOString(),
      timeline: [
        {
          id: `tl-${Date.now()}-1`,
          actorName: 'Alice Reporter',
          action: 'ISSUE_SUBMITTED',
          notes: `Problem report submitted: ${formData.title}`,
          timestamp: new Date().toISOString()
        },
        {
          id: `tl-${Date.now()}-2`,
          actorName: 'Routing Engine',
          action: 'STATUS_CHANGED',
          notes: `Context-aware routing dispatched report to ${dept?.name || 'Facilities'} supervisor queue`,
          timestamp: new Date().toISOString()
        }
      ],
      expenditures: [],
    };

    // If repeat asset tag, attach recurrence intelligence
    if (formData.assetId === 'CHILLER-CH-400') {
      newIssue.recurrenceIntelligence = {
        totalRecurrences: 3,
        assetMatchCount: 3,
        locationMatchCount: 3,
        recurrenceLevel: 'CHRONIC_REPEAT',
        chronic: true,
        recommendation: 'Chronic repeat failure: 3 similar incidents recorded in the last 90 days. Recommend comprehensive replacement rather than routine patch repair.',
        relatedIssues: [
          { id: 'h-1', referenceCode: 'PH-0982', title: 'Chiller vibration alarm', status: 'CLOSED' },
          { id: 'h-2', referenceCode: 'PH-0941', title: 'Thermal shutdown', status: 'CLOSED' }
        ]
      };
    }

    setIssues([newIssue, ...issues]);

    // Dispatch notification
    const newNotif = {
      id: `notif-${Date.now()}`,
      type: 'ISSUE_SUBMITTED',
      title: `New Ticket: ${refCode}`,
      message: `${formData.title} reported in ${formData.locationBuilding}`,
      referenceCode: refCode,
      read: false,
      createdAt: new Date().toISOString()
    };
    setNotifications([newNotif, ...notifications]);

    setIsReportModalOpen(false);
    setSelectedIssue(newIssue);
  };

  // 2. Supervisor Actions: Review, Assign, Reject, RFI
  const handleStartReview = (issueId) => {
    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;
      return {
        ...i,
        status: 'UNDER_REVIEW',
        currentStage: 'UNDER_REVIEW',
        responsibleRole: 'ROLE_SUPERVISOR',
        nextAction: 'Supervisor is reviewing report and preparing assignment or RFI',
        timeline: [
          ...i.timeline,
          {
            id: `tl-${Date.now()}`,
            actorName: 'Marcus Aurelius',
            action: 'REVIEW_STARTED',
            notes: 'Supervisor initiated triage review',
            timestamp: new Date().toISOString()
          }
        ]
      };
    }));
  };

  const handleAssignTechnician = (issueId, technicianId, instructions) => {
    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;
      return {
        ...i,
        status: 'ASSIGNED',
        assignedTechnicianName: 'Bob Builder',
        assignedTechnicianId: technicianId,
        currentStage: 'ASSIGNED',
        responsibleRole: 'ROLE_TECHNICIAN',
        blockingReason: null,
        nextAction: 'Assigned technician (Bob Builder) must accept work and start diagnosis',
        timeline: [
          ...i.timeline,
          {
            id: `tl-${Date.now()}`,
            actorName: 'Marcus Aurelius',
            action: 'TECHNICIAN_ASSIGNED',
            notes: `Assigned to Bob Builder. Instructions: ${instructions}`,
            timestamp: new Date().toISOString()
          }
        ]
      };
    }));

    setNotifications([
      {
        id: `notif-${Date.now()}`,
        type: 'ISSUE_ASSIGNED',
        title: 'Work Order Assigned',
        message: `You were assigned ticket #${issues.find(i => i.id === issueId)?.referenceCode}`,
        referenceCode: issues.find(i => i.id === issueId)?.referenceCode,
        read: false,
        createdAt: new Date().toISOString()
      },
      ...notifications
    ]);
  };

  const handleRejectIssue = (issueId, reason) => {
    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;
      return {
        ...i,
        status: 'REJECTED',
        currentStage: 'REJECTED',
        responsibleRole: 'ROLE_SUPERVISOR',
        blockingReason: `Rejected: ${reason}`,
        nextAction: 'Issue closed by supervisor decision',
        closedAt: new Date().toISOString(),
        timeline: [
          ...i.timeline,
          {
            id: `tl-${Date.now()}`,
            actorName: 'Marcus Aurelius',
            action: 'ISSUE_REJECTED',
            notes: `Rejected with reason: ${reason}`,
            timestamp: new Date().toISOString()
          }
        ]
      };
    }));
  };

  const handleRequestInfo = (issueId, question) => {
    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;
      return {
        ...i,
        status: 'MORE_INFO_REQUESTED',
        currentStage: 'MORE_INFO_REQUESTED',
        responsibleRole: 'ROLE_REPORTER',
        blockingReason: `Awaiting reporter answer: "${question}"`,
        nextAction: 'Reporter (Alice Reporter) must respond with requested details to unblock triage',
        timeline: [
          ...i.timeline,
          {
            id: `tl-${Date.now()}`,
            actorName: 'Marcus Aurelius',
            action: 'MORE_INFO_REQUESTED',
            notes: `RFI Question: ${question}`,
            timestamp: new Date().toISOString()
          }
        ]
      };
    }));
  };

  const handleProvideInfo = (issueId, response) => {
    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;
      return {
        ...i,
        status: 'UNDER_REVIEW',
        currentStage: 'UNDER_REVIEW',
        responsibleRole: 'ROLE_SUPERVISOR',
        blockingReason: null,
        nextAction: 'Supervisor is reviewing reporter provided details',
        timeline: [
          ...i.timeline,
          {
            id: `tl-${Date.now()}`,
            actorName: 'Alice Reporter',
            action: 'MORE_INFO_PROVIDED',
            notes: `Reporter Response: ${response}`,
            timestamp: new Date().toISOString()
          }
        ]
      };
    }));
  };

  // 3. Technician Actions: Start, Progress, Spend, Resolve
  const handleStartWork = (issueId) => {
    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;
      return {
        ...i,
        status: 'IN_PROGRESS',
        currentStage: 'IN_PROGRESS',
        responsibleRole: 'ROLE_TECHNICIAN',
        blockingReason: null,
        nextAction: 'Technician is actively diagnosing and implementing repair',
        timeline: [
          ...i.timeline,
          {
            id: `tl-${Date.now()}`,
            actorName: 'Bob Builder',
            action: 'STATUS_CHANGED',
            notes: 'Work started by assigned technician',
            timestamp: new Date().toISOString()
          }
        ]
      };
    }));
  };

  const handleUpdateProgress = (issueId, notes, progressPercentage) => {
    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;
      return {
        ...i,
        timeline: [
          ...i.timeline,
          {
            id: `tl-${Date.now()}`,
            actorName: 'Bob Builder',
            action: 'PROGRESS_UPDATED',
            notes: `[${progressPercentage}% Complete] ${notes}`,
            timestamp: new Date().toISOString()
          }
        ]
      };
    }));
  };

  const handleRequestExpenditure = (issueId, amount, justification) => {
    const isAutoApproved = amount <= 100.00;

    const newExpenditure = {
      id: `exp-${Date.now()}`,
      amount,
      justification,
      status: isAutoApproved ? 'APPROVED' : 'PENDING',
      requestedByName: 'Bob Builder',
      createdAt: new Date().toISOString(),
    };

    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;

      const updatedExp = [...(i.expenditures || []), newExpenditure];

      if (isAutoApproved) {
        return {
          ...i,
          expenditures: updatedExp,
          timeline: [
            ...i.timeline,
            {
              id: `tl-${Date.now()}`,
              actorName: 'System Spend Engine',
              action: 'EXPENDITURE_APPROVED',
              notes: `Expenditure of $${amount.toFixed(2)} auto-approved (within $100 limit)`,
              timestamp: new Date().toISOString()
            }
          ]
        };
      } else {
        return {
          ...i,
          status: 'AWAITING_FINANCE',
          currentStage: 'AWAITING_FINANCE',
          responsibleRole: 'ROLE_FINANCE_APPROVER',
          blockingReason: `Expenditure request of $${amount.toFixed(2)} exceeds auto-approval threshold ($100.00)`,
          nextAction: `Finance Approver must approve or decline expenditure of $${amount.toFixed(2)}`,
          expenditures: updatedExp,
          timeline: [
            ...i.timeline,
            {
              id: `tl-${Date.now()}`,
              actorName: 'Bob Builder',
              action: 'EXPENDITURE_REQUESTED',
              notes: `Requested $${amount.toFixed(2)} for ${justification}`,
              timestamp: new Date().toISOString()
            }
          ]
        };
      }
    }));

    if (!isAutoApproved) {
      setNotifications([
        {
          id: `notif-${Date.now()}`,
          type: 'EXPENDITURE_REQUESTED',
          title: 'Finance Authorization Required',
          message: `Technician requested $${amount.toFixed(2)} for ${justification}`,
          referenceCode: issues.find(i => i.id === issueId)?.referenceCode,
          read: false,
          createdAt: new Date().toISOString()
        },
        ...notifications
      ]);
    }
  };

  const handleSubmitResolution = (issueId, resolutionData) => {
    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;
      return {
        ...i,
        status: 'RESOLVED_PENDING_VERIFICATION',
        currentStage: 'RESOLVED_PENDING_VERIFICATION',
        responsibleRole: 'ROLE_SUPERVISOR',
        blockingReason: 'Pending independent verification and sign-off (Technician self-verification prohibited)',
        nextAction: 'Authorized supervisor or reporter must independently inspect work outcome',
        workSummary: resolutionData.workSummary,
        rootCause: resolutionData.rootCause,
        laborHours: resolutionData.laborHours,
        partsUsed: resolutionData.partsUsed,
        resolutionEvidenceUrl: resolutionData.resolutionEvidenceUrl,
        timeline: [
          ...i.timeline,
          {
            id: `tl-${Date.now()}`,
            actorName: 'Bob Builder',
            action: 'SUBMITTED_FOR_VERIFICATION',
            notes: `Resolution submitted: ${resolutionData.workSummary}`,
            timestamp: new Date().toISOString()
          }
        ]
      };
    }));

    setNotifications([
      {
        id: `notif-${Date.now()}`,
        type: 'RESOLUTION_SUBMITTED',
        title: 'Ready for Independent Sign-off',
        message: `Work completed by Bob Builder. Awaiting independent inspection.`,
        referenceCode: issues.find(i => i.id === issueId)?.referenceCode,
        read: false,
        createdAt: new Date().toISOString()
      },
      ...notifications
    ]);
  };

  // 4. Finance Decisions
  const handleDecideExpenditure = (issueId, expenditureId, approved, notes) => {
    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;

      const updatedExp = (i.expenditures || []).map(e => {
        if (e.id === expenditureId) {
          return { ...e, status: approved ? 'APPROVED' : 'REJECTED' };
        }
        return e;
      });

      if (approved) {
        return {
          ...i,
          status: 'IN_PROGRESS',
          currentStage: 'IN_PROGRESS',
          responsibleRole: 'ROLE_TECHNICIAN',
          blockingReason: null,
          nextAction: 'Technician may resume work with authorized parts and budget',
          expenditures: updatedExp,
          timeline: [
            ...i.timeline,
            {
              id: `tl-${Date.now()}`,
              actorName: 'Fiona Finance',
              action: 'EXPENDITURE_APPROVED',
              notes: `Finance approved expenditure. Notes: ${notes}`,
              timestamp: new Date().toISOString()
            }
          ]
        };
      } else {
        return {
          ...i,
          status: 'UNDER_REVIEW',
          currentStage: 'UNDER_SUPERVISOR_REVIEW',
          responsibleRole: 'ROLE_SUPERVISOR',
          blockingReason: `Expenditure declined by Finance: ${notes}`,
          nextAction: 'Supervisor must review alternative repair or re-scope work',
          expenditures: updatedExp,
          timeline: [
            ...i.timeline,
            {
              id: `tl-${Date.now()}`,
              actorName: 'Fiona Finance',
              action: 'EXPENDITURE_REJECTED',
              notes: `Finance declined expenditure: ${notes}`,
              timestamp: new Date().toISOString()
            }
          ]
        };
      }
    }));
  };

  // 5. Verification Sign-Off (Separation of Duties)
  const handleVerifyResolution = (issueId, approved, verificationNotes) => {
    setIssues(issues.map(i => {
      if (i.id !== issueId) return i;

      if (approved) {
        return {
          ...i,
          status: 'CLOSED',
          currentStage: 'CLOSED',
          responsibleRole: 'NONE',
          blockingReason: null,
          nextAction: 'Ticket resolved and independently signed off',
          closedAt: new Date().toISOString(),
          timeline: [
            ...i.timeline,
            {
              id: `tl-${Date.now()}`,
              actorName: currentRole === 'ROLE_SUPERVISOR' ? 'Marcus Aurelius' : 'Alice Reporter',
              action: 'VERIFICATION_PASSED',
              notes: `Independent sign-off granted: ${verificationNotes}`,
              timestamp: new Date().toISOString()
            }
          ]
        };
      } else {
        return {
          ...i,
          status: 'IN_PROGRESS',
          currentStage: 'IN_PROGRESS',
          responsibleRole: 'ROLE_TECHNICIAN',
          blockingReason: `Verification Failed: ${verificationNotes}`,
          nextAction: 'Technician must address inspection deficiency and perform rework',
          timeline: [
            ...i.timeline,
            {
              id: `tl-${Date.now()}`,
              actorName: currentRole === 'ROLE_SUPERVISOR' ? 'Marcus Aurelius' : 'Alice Reporter',
              action: 'VERIFICATION_FAILED',
              notes: `Verification rejected: ${verificationNotes}`,
              timestamp: new Date().toISOString()
            }
          ]
        };
      }
    }));
  };

  // 6. SLA Escalation Engine
  const handleTriggerSlaEscalation = async () => {
    let count = 0;
    setIssues(issues.map(i => {
      // If critical or older than 2 hours, flag breached in demonstration
      if (i.priority === 'CRITICAL' || i.referenceCode === 'PH-1002') {
        count++;
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
              notes: 'SLA deadline exceeded. High-priority escalation dispatched to department supervisor.',
              timestamp: new Date().toISOString()
            }
          ]
        };
      }
      return i;
    }));

    setNotifications([
      {
        id: `notif-${Date.now()}`,
        type: 'SLA_BREACH_WARNING',
        title: 'SLA Breach Escalation Alert',
        message: 'Critical pump maintenance #PH-1002 exceeded 4-hour threshold window.',
        referenceCode: 'PH-1002',
        read: false,
        createdAt: new Date().toISOString()
      },
      ...notifications
    ]);

    return { escalatedCount: count || 1 };
  };

  // Notification actions
  const handleMarkNotificationRead = (id) => {
    setNotifications(notifications.map(n => n.id === id ? { ...n, read: true } : n));
  };

  const handleMarkAllRead = () => {
    setNotifications(notifications.map(n => ({ ...n, read: true })));
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      <Navbar 
        currentRole={currentRole}
        setCurrentRole={setCurrentRole}
        currentUser={DEMO_USERS[currentRole.toLowerCase().replace('role_', '')] || DEMO_USERS.reporter}
        notifications={notifications}
        onMarkNotificationRead={handleMarkNotificationRead}
        onMarkAllRead={handleMarkAllRead}
        onOpenReportModal={() => setIsReportModalOpen(true)}
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />

      {/* Main Content View Container */}
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
            currentUser={DEMO_USERS[currentRole.toLowerCase().replace('role_', '')] || DEMO_USERS.reporter}
            onVerifyResolution={handleVerifyResolution}
            onSelectIssue={setSelectedIssue}
          />
        )}

        {activeTab === 'analytics' && (
          <AnalyticsView 
            issues={issues}
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
        issue={selectedIssue}
        onClose={() => setSelectedIssue(null)}
        onProvideInfo={handleProvideInfo}
        currentRole={currentRole}
      />
    </div>
  );
}
