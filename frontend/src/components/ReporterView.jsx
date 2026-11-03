import React, { useState } from 'react';
import { 
  FileText, 
  Clock, 
  CheckCircle2, 
  AlertTriangle, 
  Search, 
  Filter, 
  Plus, 
  MapPin, 
  Cpu, 
  ArrowRight
} from 'lucide-react';

export default function ReporterView({ 
  issues, 
  onSelectIssue, 
  onOpenReportModal 
}) {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const myReports = issues.filter(issue => {
    const matchesSearch = 
      issue.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      issue.referenceCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (issue.assetId && issue.assetId.toLowerCase().includes(searchTerm.toLowerCase())) ||
      issue.locationRoom.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesStatus = statusFilter === 'ALL' || issue.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const totalReported = issues.length;
  const activeIssues = issues.filter(i => i.status !== 'CLOSED' && i.status !== 'REJECTED').length;
  const resolvedIssues = issues.filter(i => i.status === 'CLOSED').length;
  const awaitingAction = issues.filter(i => i.status === 'MORE_INFO_REQUESTED').length;

  return (
    <div className="view-container">
      {/* Top Banner / Metrics */}
      <div className="metric-grid">
        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--indigo-500)' }}>
          <div className="metric-label">
            <span>Total Reported</span>
            <FileText size={16} color="var(--indigo-500)" />
          </div>
          <div className="metric-value">{totalReported}</div>
          <div className="metric-subtext">Cumulative organizational problem reports</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--amber-500)' }}>
          <div className="metric-label">
            <span>Active In Triage</span>
            <Clock size={16} color="var(--amber-500)" />
          </div>
          <div className="metric-value">{activeIssues}</div>
          <div className="metric-subtext">Issues moving through review and repair</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--emerald-500)' }}>
          <div className="metric-label">
            <span>Verified Closed</span>
            <CheckCircle2 size={16} color="var(--emerald-500)" />
          </div>
          <div className="metric-value">{resolvedIssues}</div>
          <div className="metric-subtext">Independently confirmed and signed off</div>
        </div>

        <div className="metric-card glass-panel" style={{ '--accent-color': 'var(--cyan-500)' }}>
          <div className="metric-label">
            <span>RFI Actions Pending</span>
            <AlertTriangle size={16} color="var(--cyan-500)" />
          </div>
          <div className="metric-value">{awaitingAction}</div>
          <div className="metric-subtext">Requires reporter information to proceed</div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="glass-panel" style={{ padding: '16px 20px', marginBottom: '20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '16px', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flex: '1', minWidth: '280px' }}>
          <Search size={18} color="var(--text-muted)" />
          <input 
            type="text" 
            placeholder="Search by reference (PH-1001), keyword, room, or asset tag..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ background: 'transparent', border: 'none', padding: '6px 0', fontSize: '0.92rem' }}
            id="input-search-reports"
          />
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Filter size={16} color="var(--text-muted)" />
          <select 
            value={statusFilter} 
            onChange={(e) => setStatusFilter(e.target.value)}
            style={{ width: 'auto', background: 'var(--bg-primary)', padding: '6px 12px' }}
            id="select-status-filter"
          >
            <option value="ALL">All Statuses</option>
            <option value="UNDER_REVIEW">Under Review</option>
            <option value="MORE_INFO_REQUESTED">More Info Requested</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="AWAITING_FINANCE">Awaiting Finance</option>
            <option value="RESOLVED_PENDING_VERIFICATION">Resolved (Pending Sign-off)</option>
            <option value="CLOSED">Closed</option>
            <option value="REJECTED">Rejected</option>
          </select>

          <button 
            className="btn-primary" 
            onClick={onOpenReportModal}
            id="btn-report-modal-trigger"
          >
            <Plus size={16} />
            <span>New Problem Report</span>
          </button>
        </div>
      </div>

      {/* My Reports Table */}
      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Reference</th>
              <th>Priority</th>
              <th>Title & Category</th>
              <th>Location & Asset</th>
              <th>Current Stage</th>
              <th>SLA Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {myReports.length === 0 ? (
              <tr>
                <td colSpan="7" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
                  No matching problem reports found.
                </td>
              </tr>
            ) : (
              myReports.map(issue => (
                <tr key={issue.id} onClick={() => onSelectIssue(issue)}>
                  <td>
                    <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 700, color: '#A5B4FC', fontSize: '0.9rem' }}>
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
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                      {issue.departmentName} &bull; {issue.categoryName}
                    </div>
                  </td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.85rem' }}>
                      <MapPin size={13} color="var(--text-muted)" />
                      <span>{issue.locationBuilding}, {issue.locationRoom}</span>
                    </div>
                    {issue.assetId && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.78rem', color: 'var(--cyan-500)', marginTop: '2px' }}>
                        <Cpu size={12} />
                        <span>{issue.assetId}</span>
                      </div>
                    )}
                  </td>
                  <td>
                    <span className={`status-badge ${issue.status}`}>
                      {issue.status.replace(/_/g, ' ')}
                    </span>
                    {issue.blockingReason && (
                      <div style={{ fontSize: '0.74rem', color: '#FCD34D', marginTop: '4px', maxWidth: '240px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        ⚠ {issue.blockingReason}
                      </div>
                    )}
                  </td>
                  <td>
                    <div style={{ fontSize: '0.82rem', fontWeight: 600, color: issue.slaStatus === 'BREACHED' ? '#F87171' : issue.slaStatus === 'AT_RISK' ? '#FBBF24' : '#34D399' }}>
                      {issue.slaStatus || 'ON_TRACK'}
                    </div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                      {issue.slaRemainingMinutes ? `${Math.round(issue.slaRemainingMinutes / 60)}h remaining` : 'Within window'}
                    </div>
                  </td>
                  <td>
                    <button 
                      className="btn-secondary" 
                      style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                      onClick={(e) => {
                        e.stopPropagation();
                        onSelectIssue(issue);
                      }}
                    >
                      <span>Track</span>
                      <ArrowRight size={13} />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
