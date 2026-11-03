import React, { useState } from 'react';
import { 
  Wrench, 
  Play, 
  TrendingUp, 
  DollarSign, 
  CheckCircle2, 
  Clock, 
  MapPin, 
  Cpu, 
  AlertTriangle,
  ArrowRight
} from 'lucide-react';

export default function TechnicianView({ 
  issues, 
  onSelectIssue, 
  onStartWork, 
  onUpdateProgress, 
  onRequestExpenditure, 
  onSubmitResolution 
}) {
  const [selectedIssue, setSelectedIssue] = useState(null);
  const [activeModal, setActiveModal] = useState(null); // 'PROGRESS', 'SPEND', 'RESOLVE'

  // Modal form states
  const [progressNotes, setProgressNotes] = useState('');
  const [progressPercent, setProgressPercent] = useState(50);

  const [spendAmount, setSpendAmount] = useState('');
  const [spendJustification, setSpendJustification] = useState('');

  const [workSummary, setWorkSummary] = useState('');
  const [rootCause, setRootCause] = useState('');
  const [laborHours, setLaborHours] = useState('2.0');
  const [partsUsed, setPartsUsed] = useState('');
  const [evidenceUrl, setEvidenceUrl] = useState('');

  // Filter issues assigned to technician or actionable in maintenance
  const techWorkboard = issues.filter(i => 
    i.status === 'ASSIGNED' || 
    i.status === 'IN_PROGRESS' || 
    i.status === 'AWAITING_FINANCE' || 
    i.status === 'RESOLVED_PENDING_VERIFICATION'
  );

  // Priority sorting
  const priorityRank = { CRITICAL: 1, HIGH: 2, MEDIUM: 3, LOW: 4 };
  techWorkboard.sort((a, b) => (priorityRank[a.priority] || 5) - (priorityRank[b.priority] || 5));

  const assignedCount = issues.filter(i => i.status === 'ASSIGNED').length;
  const inProgressCount = issues.filter(i => i.status === 'IN_PROGRESS').length;
  const awaitingFinanceCount = issues.filter(i => i.status === 'AWAITING_FINANCE').length;
  const resolvedPendingVerification = issues.filter(i => i.status === 'RESOLVED_PENDING_VERIFICATION').length;

  const handleOpenAction = (issue, type) => {
    setSelectedIssue(issue);
    setActiveModal(type);
    if (type === 'PROGRESS') {
      setProgressNotes('');
      setProgressPercent(50);
    } else if (type === 'SPEND') {
      setSpendAmount('');
      setSpendJustification('');
    } else if (type === 'RESOLVE') {
      setWorkSummary('');
      setRootCause('');
      setLaborHours('2.0');
      setPartsUsed('');
      setEvidenceUrl('https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&q=80&w=800');
    }
  };

  const handleSaveProgress = () => {
    if (!progressNotes.trim()) {
      alert('Please enter progress notes.');
      return;
    }
    onUpdateProgress(selectedIssue.id, progressNotes, Number(progressPercent));
    setActiveModal(null);
  };

  const handleSaveSpend = () => {
    const amt = parseFloat(spendAmount);
    if (isNaN(amt) || amt <= 0) {
      alert('Please enter a valid amount.');
      return;
    }
    if (!spendJustification.trim()) {
      alert('Please provide justification for expenditure.');
      return;
    }
    onRequestExpenditure(selectedIssue.id, amt, spendJustification);
    setActiveModal(null);
  };

  const handleSaveResolve = () => {
    if (!workSummary.trim() || workSummary.length < 10) {
      alert('Please provide work summary (min 10 characters).');
      return;
    }
    onSubmitResolution(selectedIssue.id, {
      workSummary,
      rootCause,
      laborHours: parseFloat(laborHours) || 1.0,
      partsUsed,
      resolutionEvidenceUrl: evidenceUrl
    });
    setActiveModal(null);
  };

  return (
    <div className="view-container">
      {/* Metrics */}
      <div className="metric-grid">
        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--indigo-500)' }}>
          <div className="metric-label">
            <span>New Assigned</span>
            <Clock size={16} color="var(--indigo-500)" />
          </div>
          <div className="metric-value">{assignedCount}</div>
          <div className="metric-subtext">Assigned tasks waiting to begin</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--cyan-500)' }}>
          <div className="metric-label">
            <span>Active In Progress</span>
            <Wrench size={16} color="var(--cyan-500)" />
          </div>
          <div className="metric-value">{inProgressCount}</div>
          <div className="metric-subtext">Active diagnosis and repair tasks</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--amber-500)' }}>
          <div className="metric-label">
            <span>Awaiting Finance</span>
            <DollarSign size={16} color="var(--amber-500)" />
          </div>
          <div className="metric-value">{awaitingFinanceCount}</div>
          <div className="metric-subtext">Spend exceeding $100 limit in review</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--emerald-500)' }}>
          <div className="metric-label">
            <span>Submitted for Verification</span>
            <CheckCircle2 size={16} color="var(--emerald-500)" />
          </div>
          <div className="metric-value">{resolvedPendingVerification}</div>
          <div className="metric-subtext">Awaiting independent supervisor/reporter sign-off</div>
        </div>
      </div>

      {/* Workboard Cards */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {techWorkboard.length === 0 ? (
          <div className="glass-panel" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            No active work orders assigned to you at this time.
          </div>
        ) : (
          techWorkboard.map(issue => (
            <div 
              key={issue.id} 
              className="glass-panel"
              style={{
                padding: '20px',
                borderLeft: issue.priority === 'CRITICAL' ? '4px solid var(--rose-500)' : issue.priority === 'HIGH' ? '4px solid var(--amber-500)' : '4px solid var(--indigo-500)',
                transition: 'transform 0.15s ease',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: '12px', marginBottom: '12px' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: '#A5B4FC' }}>
                      {issue.referenceCode}
                    </span>
                    <span className={`priority-chip ${issue.priority}`}>
                      {issue.priority}
                    </span>
                    <span className={`status-badge ${issue.status}`}>
                      {issue.status.replace(/_/g, ' ')}
                    </span>
                  </div>
                  <h3 style={{ fontSize: '1.05rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                    {issue.title}
                  </h3>
                </div>

                <div style={{ display: 'flex', gap: '8px' }}>
                  {issue.status === 'ASSIGNED' && (
                    <button 
                      className="btn-primary" 
                      onClick={() => onStartWork(issue.id)}
                    >
                      <Play size={14} />
                      <span>Start Work</span>
                    </button>
                  )}

                  {issue.status === 'IN_PROGRESS' && (
                    <>
                      <button 
                        className="btn-secondary" 
                        onClick={() => handleOpenAction(issue, 'PROGRESS')}
                      >
                        <TrendingUp size={14} />
                        <span>Log Progress</span>
                      </button>

                      <button 
                        className="btn-secondary" 
                        onClick={() => handleOpenAction(issue, 'SPEND')}
                      >
                        <DollarSign size={14} />
                        <span>Request Spend</span>
                      </button>

                      <button 
                        className="btn-success" 
                        onClick={() => handleOpenAction(issue, 'RESOLVE')}
                      >
                        <CheckCircle2 size={14} />
                        <span>Complete & Resolve</span>
                      </button>
                    </>
                  )}

                  {issue.status === 'AWAITING_FINANCE' && (
                    <div className="blocker-alert" style={{ margin: 0, padding: '6px 12px' }}>
                      <AlertTriangle size={14} />
                      <span>Blocked: Awaiting Finance Approval</span>
                    </div>
                  )}

                  {issue.status === 'RESOLVED_PENDING_VERIFICATION' && (
                    <div style={{ fontSize: '0.85rem', color: '#C084FC', display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <CheckCircle2 size={16} />
                      <span>Submitted: Pending Independent Sign-off</span>
                    </div>
                  )}

                  <button 
                    className="btn-secondary" 
                    onClick={() => onSelectIssue(issue)}
                    title="View Details"
                  >
                    <span>Details</span>
                    <ArrowRight size={13} />
                  </button>
                </div>
              </div>

              <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', marginBottom: '14px', lineHeight: 1.5 }}>
                {issue.description}
              </p>

              <div style={{ display: 'flex', alignItems: 'center', gap: '20px', flexWrap: 'wrap', fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                  <MapPin size={13} />
                  {issue.locationBuilding}, {issue.locationRoom}
                </span>
                {issue.assetId && (
                  <span style={{ display: 'flex', alignItems: 'center', gap: '5px', color: 'var(--cyan-500)' }}>
                    <Cpu size={13} />
                    Asset: {issue.assetId}
                  </span>
                )}
                <span>Department: {issue.departmentName}</span>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Progress Dialog */}
      {activeModal === 'PROGRESS' && (
        <div className="modal-backdrop">
          <div className="modal-dialog">
            <div className="modal-header">
              <span style={{ fontWeight: 700 }}>Log Progress for {selectedIssue?.referenceCode}</span>
              <button onClick={() => setActiveModal(null)} style={{ background: 'transparent', color: 'var(--text-muted)' }}>✕</button>
            </div>
            <div className="modal-body">
              <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Progress Percentage: {progressPercent}%
              </label>
              <input 
                type="range" 
                min="0" 
                max="100" 
                value={progressPercent} 
                onChange={e => setProgressPercent(e.target.value)}
                style={{ marginBottom: '16px' }}
              />

              <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Progress & Diagnostic Notes:
              </label>
              <textarea 
                rows={4} 
                placeholder="Detail diagnosis findings, parts inspected, or steps completed..."
                value={progressNotes}
                onChange={e => setProgressNotes(e.target.value)}
              />
            </div>
            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setActiveModal(null)}>Cancel</button>
              <button className="btn-primary" onClick={handleSaveProgress}>Save Progress Update</button>
            </div>
          </div>
        </div>
      )}

      {/* Spend Request Dialog */}
      {activeModal === 'SPEND' && (
        <div className="modal-backdrop">
          <div className="modal-dialog">
            <div className="modal-header">
              <span style={{ fontWeight: 700 }}>Request Expenditure for {selectedIssue?.referenceCode}</span>
              <button onClick={() => setActiveModal(null)} style={{ background: 'transparent', color: 'var(--text-muted)' }}>✕</button>
            </div>
            <div className="modal-body">
              <div className="blocker-alert" style={{ marginBottom: '16px' }}>
                <DollarSign size={16} />
                <span>Threshold Policy: Spend &le; $100.00 is auto-approved without blocking. Spend &gt; $100.00 transitions issue to AWAITING_FINANCE.</span>
              </div>

              <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Required Amount ($ USD):
              </label>
              <input 
                type="number" 
                step="0.01" 
                placeholder="e.g. 380.00" 
                value={spendAmount}
                onChange={e => setSpendAmount(e.target.value)}
                style={{ marginBottom: '16px' }}
              />

              <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Expenditure Justification & Parts Quote:
              </label>
              <textarea 
                rows={3} 
                placeholder="Explain why this part or contractor service is required to complete the repair..."
                value={spendJustification}
                onChange={e => setSpendJustification(e.target.value)}
              />
            </div>
            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setActiveModal(null)}>Cancel</button>
              <button className="btn-primary" onClick={handleSaveSpend}>Submit Spend Request</button>
            </div>
          </div>
        </div>
      )}

      {/* Resolution Submission Dialog */}
      {activeModal === 'RESOLVE' && (
        <div className="modal-backdrop">
          <div className="modal-dialog">
            <div className="modal-header">
              <span style={{ fontWeight: 700 }}>Submit Resolution Evidence for {selectedIssue?.referenceCode}</span>
              <button onClick={() => setActiveModal(null)} style={{ background: 'transparent', color: 'var(--text-muted)' }}>✕</button>
            </div>
            <div className="modal-body">
              <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Work Summary & Completed Actions:
              </label>
              <textarea 
                rows={3} 
                placeholder="Describe exactly what was repaired or replaced..."
                value={workSummary}
                onChange={e => setWorkSummary(e.target.value)}
                style={{ marginBottom: '14px' }}
              />

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '14px' }}>
                <div>
                  <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                    Root Cause:
                  </label>
                  <input 
                    type="text" 
                    placeholder="e.g. Galvanic corrosion, electrical surge" 
                    value={rootCause}
                    onChange={e => setRootCause(e.target.value)}
                  />
                </div>
                <div>
                  <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                    Labor Hours:
                  </label>
                  <input 
                    type="number" 
                    step="0.5" 
                    value={laborHours}
                    onChange={e => setLaborHours(e.target.value)}
                  />
                </div>
              </div>

              <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Parts & Materials Consumed:
              </label>
              <input 
                type="text" 
                placeholder="e.g. 1x PEX Coupler, 2x Clamps" 
                value={partsUsed}
                onChange={e => setPartsUsed(e.target.value)}
                style={{ marginBottom: '14px' }}
              />

              <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Resolution Evidence URL (Photo / Work Order):
              </label>
              <input 
                type="text" 
                value={evidenceUrl}
                onChange={e => setEvidenceUrl(e.target.value)}
              />
            </div>
            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setActiveModal(null)}>Cancel</button>
              <button className="btn-success" onClick={handleSaveResolve}>Submit for Independent Verification</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
