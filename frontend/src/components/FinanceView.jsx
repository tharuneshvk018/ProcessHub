import React, { useState } from 'react';
import { 
  DollarSign, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  TrendingUp
} from 'lucide-react';

export default function FinanceView({ 
  issues, 
  onDecideExpenditure, 
  onSelectIssue 
}) {
  const [selectedIssue, setSelectedIssue] = useState(null);
  const [selectedExpenditure, setSelectedExpenditure] = useState(null);
  const [decisionType, setDecisionType] = useState(null); // 'APPROVE', 'REJECT'
  const [decisionNotes, setDecisionNotes] = useState('');

  // Collect all issues awaiting finance or with pending expenditures
  const pendingFinanceIssues = issues.filter(i => 
    i.status === 'AWAITING_FINANCE' || 
    (i.expenditures && i.expenditures.some(e => e.status === 'PENDING'))
  );

  const pendingRequests = [];
  pendingFinanceIssues.forEach(issue => {
    (issue.expenditures || []).forEach(exp => {
      if (exp.status === 'PENDING') {
        pendingRequests.push({ issue, expenditure: exp });
      }
    });
  });

  const totalPendingAmount = pendingRequests.reduce((sum, r) => sum + (r.expenditure.amount || 0), 0);

  const handleOpenDecision = (issue, expenditure, type) => {
    setSelectedIssue(issue);
    setSelectedExpenditure(expenditure);
    setDecisionType(type);
    setDecisionNotes(type === 'APPROVE' ? 'Approved against Facilities Contingency Budget Code #FAC-409' : '');
  };

  const handleConfirmDecision = () => {
    if (!selectedIssue || !selectedExpenditure) return;

    if (decisionType === 'REJECT' && (!decisionNotes || decisionNotes.trim().length < 5)) {
      alert('Please specify rejection rationale for finance audit trail.');
      return;
    }

    onDecideExpenditure(
      selectedIssue.id, 
      selectedExpenditure.id, 
      decisionType === 'APPROVE', 
      decisionNotes
    );

    setDecisionType(null);
    setSelectedIssue(null);
    setSelectedExpenditure(null);
  };

  return (
    <div className="view-container">
      {/* Finance Metrics */}
      <div className="metric-grid">
        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--amber-500)' }}>
          <div className="metric-label">
            <span>Pending Authorizations</span>
            <Clock size={16} color="var(--amber-500)" />
          </div>
          <div className="metric-value">{pendingRequests.length}</div>
          <div className="metric-subtext">Expenditure requests exceeding $100 limit</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--rose-500)' }}>
          <div className="metric-label">
            <span>Total Pending Amount</span>
            <DollarSign size={16} color="var(--rose-500)" />
          </div>
          <div className="metric-value">${totalPendingAmount.toFixed(2)}</div>
          <div className="metric-subtext">Unapproved maintenance capital</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--emerald-500)' }}>
          <div className="metric-label">
            <span>YTD Approved Spend</span>
            <CheckCircle2 size={16} color="var(--emerald-500)" />
          </div>
          <div className="metric-value">$14,850.00</div>
          <div className="metric-subtext">Cumulative authorized maintenance purchases</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--indigo-500)' }}>
          <div className="metric-label">
            <span>Auto-Approved (&le; $100)</span>
            <TrendingUp size={16} color="var(--indigo-500)" />
          </div>
          <div className="metric-value">42 Requests</div>
          <div className="metric-subtext">Expedited without operational delay</div>
        </div>
      </div>

      {/* Finance Queue Table */}
      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Issue Reference</th>
              <th>Amount ($ USD)</th>
              <th>Justification</th>
              <th>Requested By</th>
              <th>Department</th>
              <th>Finance Decision</th>
            </tr>
          </thead>
          <tbody>
            {pendingRequests.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
                  All expenditure requests have been authorized. No items pending in Finance Queue.
                </td>
              </tr>
            ) : (
              pendingRequests.map(({ issue, expenditure }) => (
                <tr key={expenditure.id} onClick={() => onSelectIssue(issue)}>
                  <td>
                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: '#A5B4FC' }}>
                      {issue.referenceCode}
                    </span>
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
                      {issue.title}
                    </div>
                  </td>
                  <td>
                    <span style={{ fontSize: '1.1rem', fontWeight: 700, color: '#FCD34D' }}>
                      ${(expenditure.amount || 0).toFixed(2)}
                    </span>
                  </td>
                  <td style={{ maxWidth: '300px' }}>
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-primary)', lineHeight: 1.4 }}>
                      {expenditure.justification}
                    </p>
                  </td>
                  <td>
                    <div style={{ fontSize: '0.85rem' }}>{expenditure.requestedByName || 'Karthik Raj'}</div>
                    <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>Technician</div>
                  </td>
                  <td>
                    <span style={{ fontSize: '0.85rem' }}>{issue.departmentName}</span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '8px' }} onClick={e => e.stopPropagation()}>
                      <button 
                        className="btn-success" 
                        style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                        onClick={() => handleOpenDecision(issue, expenditure, 'APPROVE')}
                      >
                        <CheckCircle2 size={14} />
                        <span>Approve</span>
                      </button>

                      <button 
                        className="btn-danger" 
                        style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                        onClick={() => handleOpenDecision(issue, expenditure, 'REJECT')}
                      >
                        <XCircle size={14} />
                        <span>Decline</span>
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Decision Modal */}
      {decisionType && selectedIssue && selectedExpenditure && (
        <div className="modal-backdrop">
          <div className="modal-dialog">
            <div className="modal-header">
              <span style={{ fontWeight: 700, fontSize: '1.05rem' }}>
                {decisionType === 'APPROVE' ? 'Authorize Expenditure Request' : 'Decline Expenditure Request'}
              </span>
              <button onClick={() => setDecisionType(null)} style={{ background: 'transparent', color: 'var(--text-muted)' }}>✕</button>
            </div>

            <div className="modal-body">
              <div style={{ marginBottom: '16px', background: 'var(--bg-surface)', padding: '14px', borderRadius: 'var(--radius-md)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                  <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>Ticket Reference:</span>
                  <span style={{ fontWeight: 600 }}>{selectedIssue.referenceCode} - {selectedIssue.title}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                  <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>Requested Amount:</span>
                  <span style={{ fontWeight: 700, color: '#FCD34D', fontSize: '1.05rem' }}>${(selectedExpenditure.amount || 0).toFixed(2)} USD</span>
                </div>
                <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginBottom: '4px' }}>Justification:</div>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{selectedExpenditure.justification}</p>
              </div>

              <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                {decisionType === 'APPROVE' ? 'Accounting Code / Budget Authorization Notes:' : 'Operational Reason for Rejection:'}
              </label>
              <textarea 
                rows={3} 
                value={decisionNotes}
                onChange={e => setDecisionNotes(e.target.value)}
                placeholder={decisionType === 'APPROVE' ? 'e.g. Approved against Facilities Q4 budget code...' : 'e.g. Exceeds department discretionary allocation, seek quote for alternatives...'}
              />
            </div>

            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setDecisionType(null)}>Cancel</button>
              <button 
                className={decisionType === 'APPROVE' ? 'btn-success' : 'btn-danger'}
                onClick={handleConfirmDecision}
              >
                {decisionType === 'APPROVE' ? 'Confirm Approval & Unblock Work' : 'Decline & Route to Supervisor'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
