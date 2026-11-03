import React, { useState } from 'react';
import { 
  Users, 
  AlertCircle, 
  HelpCircle, 
  XCircle, 
  UserPlus
} from 'lucide-react';

export default function SupervisorView({ 
  issues, 
  departments, 
  onSelectIssue, 
  onAssignTechnician, 
  onRejectIssue, 
  onRequestInfo 
}) {
  const [selectedDept, setSelectedDept] = useState('ALL');
  const [selectedIssueForAction, setSelectedIssueForAction] = useState(null);
  const [actionType, setActionType] = useState(null); // 'ASSIGN', 'REJECT', 'RFI'
  
  // Dialog state
  const [assigneeId, setAssigneeId] = useState('u-bob');
  const [assignInstructions, setAssignInstructions] = useState('');
  const [rejectionReason, setRejectionReason] = useState('');
  const [rfiQuestion, setRfiQuestion] = useState('');

  const supervisorQueue = issues.filter(issue => {
    const isDeptMatch = selectedDept === 'ALL' || issue.departmentId === selectedDept;
    const isUnderReview = issue.status === 'UNDER_REVIEW' || issue.status === 'SUBMITTED' || issue.status === 'MORE_INFO_REQUESTED';
    return isDeptMatch && isUnderReview;
  });

  const pendingReview = issues.filter(i => i.status === 'SUBMITTED' || i.status === 'UNDER_REVIEW').length;
  const unassigned = issues.filter(i => !i.assignedTechnicianName && i.status !== 'CLOSED' && i.status !== 'REJECTED').length;
  const awaitingRfi = issues.filter(i => i.status === 'MORE_INFO_REQUESTED').length;
  const criticalCount = issues.filter(i => i.priority === 'CRITICAL' && i.status !== 'CLOSED' && i.status !== 'REJECTED').length;

  const handleOpenAction = (issue, type) => {
    setSelectedIssueForAction(issue);
    setActionType(type);
    setRejectionReason('');
    setRfiQuestion('');
    setAssignInstructions('');
  };

  const handleConfirmAction = () => {
    if (!selectedIssueForAction) return;

    if (actionType === 'ASSIGN') {
      onAssignTechnician(selectedIssueForAction.id, assigneeId, assignInstructions || 'Standard dispatch');
    } else if (actionType === 'REJECT') {
      if (!rejectionReason || rejectionReason.trim().length < 10) {
        alert('Rejection reason must be at least 10 characters.');
        return;
      }
      onRejectIssue(selectedIssueForAction.id, rejectionReason);
    } else if (actionType === 'RFI') {
      if (!rfiQuestion || rfiQuestion.trim().length < 5) {
        alert('Please specify the question for the reporter.');
        return;
      }
      onRequestInfo(selectedIssueForAction.id, rfiQuestion);
    }

    setActionType(null);
    setSelectedIssueForAction(null);
  };

  return (
    <div className="view-container">
      {/* Supervisor Metrics */}
      <div className="metric-grid">
        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--indigo-500)' }}>
          <div className="metric-label">
            <span>Pending Review</span>
            <AlertCircle size={16} color="var(--indigo-500)" />
          </div>
          <div className="metric-value">{pendingReview}</div>
          <div className="metric-subtext">Reports awaiting supervisor triage decision</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--amber-500)' }}>
          <div className="metric-label">
            <span>Unassigned Work</span>
            <Users size={16} color="var(--amber-500)" />
          </div>
          <div className="metric-value">{unassigned}</div>
          <div className="metric-subtext">Approved tasks pending technician assignment</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--cyan-500)' }}>
          <div className="metric-label">
            <span>Awaiting Reporter Info</span>
            <HelpCircle size={16} color="var(--cyan-500)" />
          </div>
          <div className="metric-value">{awaitingRfi}</div>
          <div className="metric-subtext">Blocked on reporter RFI responses</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--rose-500)' }}>
          <div className="metric-label">
            <span>Critical Safety / Urgent</span>
            <AlertCircle size={16} color="var(--rose-500)" />
          </div>
          <div className="metric-value">{criticalCount}</div>
          <div className="metric-subtext">Requires expedited supervisor escalation</div>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="glass-panel" style={{ padding: '14px 20px', marginBottom: '20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>
            Filter Department:
          </span>
          <button 
            className={`btn-secondary ${selectedDept === 'ALL' ? 'active' : ''}`}
            style={{ padding: '6px 14px', fontSize: '0.82rem', background: selectedDept === 'ALL' ? 'var(--bg-surface-elevated)' : 'transparent' }}
            onClick={() => setSelectedDept('ALL')}
          >
            All Departments
          </button>
          {departments.map(d => (
            <button 
              key={d.id}
              className={`btn-secondary ${selectedDept === d.id ? 'active' : ''}`}
              style={{ padding: '6px 14px', fontSize: '0.82rem', background: selectedDept === d.id ? 'var(--bg-surface-elevated)' : 'transparent' }}
              onClick={() => setSelectedDept(d.id)}
            >
              {d.name}
            </button>
          ))}
        </div>
      </div>

      {/* Triage Queue Table */}
      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Reference</th>
              <th>Priority</th>
              <th>Problem Description</th>
              <th>Location & Category</th>
              <th>Current Stage</th>
              <th>Triage Actions</th>
            </tr>
          </thead>
          <tbody>
            {supervisorQueue.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
                  All department queue items have been triaged and assigned.
                </td>
              </tr>
            ) : (
              supervisorQueue.map(issue => (
                <tr key={issue.id} onClick={() => onSelectIssue(issue)}>
                  <td>
                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: '#A5B4FC' }}>
                      {issue.referenceCode}
                    </span>
                  </td>
                  <td>
                    <span className={`priority-chip ${issue.priority}`}>
                      {issue.priority}
                    </span>
                  </td>
                  <td>
                    <div style={{ fontWeight: 600, color: 'var(--text-primary)', marginBottom: '2px' }}>
                      {issue.title}
                    </div>
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
                      Reported by: {issue.reporterName}
                    </div>
                  </td>
                  <td>
                    <div style={{ fontSize: '0.85rem' }}>{issue.locationBuilding}, {issue.locationRoom}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{issue.categoryName}</div>
                  </td>
                  <td>
                    <span className={`status-badge ${issue.status}`}>
                      {issue.status.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '6px' }} onClick={e => e.stopPropagation()}>
                      <button 
                        className="btn-primary" 
                        style={{ padding: '6px 10px', fontSize: '0.78rem' }}
                        onClick={() => handleOpenAction(issue, 'ASSIGN')}
                        title="Assign to maintenance team"
                      >
                        <UserPlus size={13} />
                        <span>Assign</span>
                      </button>

                      <button 
                        className="btn-secondary" 
                        style={{ padding: '6px 10px', fontSize: '0.78rem' }}
                        onClick={() => handleOpenAction(issue, 'RFI')}
                        title="Request more information"
                      >
                        <HelpCircle size={13} />
                        <span>RFI</span>
                      </button>

                      <button 
                        className="btn-danger" 
                        style={{ padding: '6px 10px', fontSize: '0.78rem' }}
                        onClick={() => handleOpenAction(issue, 'REJECT')}
                        title="Reject report with reason"
                      >
                        <XCircle size={13} />
                        <span>Reject</span>
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Action Dialog Modal */}
      {actionType && selectedIssueForAction && (
        <div className="modal-backdrop">
          <div className="modal-dialog">
            <div className="modal-header">
              <span style={{ fontWeight: 700, fontSize: '1.05rem' }}>
                {actionType === 'ASSIGN' && `Assign Technician for ${selectedIssueForAction.referenceCode}`}
                {actionType === 'REJECT' && `Reject Report ${selectedIssueForAction.referenceCode}`}
                {actionType === 'RFI' && `Request Information for ${selectedIssueForAction.referenceCode}`}
              </span>
              <button 
                onClick={() => setActionType(null)} 
                style={{ background: 'transparent', color: 'var(--text-muted)' }}
              >
                ✕
              </button>
            </div>

            <div className="modal-body">
              {actionType === 'ASSIGN' && (
                <div>
                  <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                    Select Technician (with active workload):
                  </label>
                  <select 
                    value={assigneeId} 
                    onChange={e => setAssigneeId(e.target.value)}
                    style={{ marginBottom: '16px' }}
                  >
                    <option value="u-bob">Karthik Raj (Facilities &bull; 2 active tasks)</option>
                    <option value="u-dave">Rohan Patel (Electrical &bull; 1 active task)</option>
                    <option value="u-carlos">Suresh Menon (IT Infrastructure &bull; 0 active tasks)</option>
                  </select>

                  <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                    Work Instructions / Priority Directive:
                  </label>
                  <textarea 
                    rows={3} 
                    placeholder="Enter specific instructions or diagnostic directives for the assigned technician..."
                    value={assignInstructions}
                    onChange={e => setAssignInstructions(e.target.value)}
                  />
                </div>
              )}

              {actionType === 'REJECT' && (
                <div>
                  <div className="blocker-alert" style={{ marginBottom: '16px' }}>
                    <AlertCircle size={16} />
                    <span>Mandatory Rejection Rule: An auditable explanation must be provided to the reporter.</span>
                  </div>
                  <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                    Rejection Reason (minimum 10 characters):
                  </label>
                  <textarea 
                    rows={4} 
                    placeholder="Provide explicit operational rationale for rejection (e.g. duplicate report, out of scope, unsupported personal equipment)..."
                    value={rejectionReason}
                    onChange={e => setRejectionReason(e.target.value)}
                  />
                </div>
              )}

              {actionType === 'RFI' && (
                <div>
                  <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', marginBottom: '14px' }}>
                    Requesting additional information moves the ticket to <strong>MORE_INFO_REQUESTED</strong> and notifies the reporter to clarify before triage resumes.
                  </p>
                  <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                    Specific Question for Reporter:
                  </label>
                  <textarea 
                    rows={3} 
                    placeholder="e.g. Please specify the room number or circuit breaker panel number..."
                    value={rfiQuestion}
                    onChange={e => setRfiQuestion(e.target.value)}
                  />
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setActionType(null)}>Cancel</button>
              <button 
                className={actionType === 'REJECT' ? 'btn-danger' : 'btn-primary'}
                onClick={handleConfirmAction}
              >
                {actionType === 'ASSIGN' && 'Confirm Assignment'}
                {actionType === 'REJECT' && 'Confirm Rejection'}
                {actionType === 'RFI' && 'Send RFI Question'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
