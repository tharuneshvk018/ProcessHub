import React, { useState } from 'react';
import { 
  Shield, 
  Bell, 
  PlusCircle, 
  UserCheck, 
  Building2, 
  Check, 
  ExternalLink,
  Layers,
  ChevronDown
} from 'lucide-react';

export default function Navbar({ 
  currentRole, 
  setCurrentRole, 
  currentUser, 
  notifications, 
  onMarkNotificationRead, 
  onMarkAllRead, 
  onOpenReportModal,
  activeTab,
  setActiveTab
}) {
  const [showNotifs, setShowNotifs] = useState(false);
  const [showRoleMenu, setShowRoleMenu] = useState(false);

  const unreadCount = notifications.filter(n => !n.read).length;

  const roles = [
    { key: 'ROLE_REPORTER', label: 'Alice (Reporter)', roleName: 'Reporter' },
    { key: 'ROLE_SUPERVISOR', label: 'Marcus (Supervisor)', roleName: 'Supervisor' },
    { key: 'ROLE_TECHNICIAN', label: 'Bob (Technician)', roleName: 'Technician' },
    { key: 'ROLE_FINANCE_APPROVER', label: 'Fiona (Finance)', roleName: 'Finance Approver' },
    { key: 'ROLE_ADMIN', label: 'Arthur (Admin)', roleName: 'Administrator' },
  ];

  return (
    <header className="app-header">
      {/* Brand & Identity */}
      <div className="brand-section">
        <div className="brand-logo-badge">
          <Shield size={22} color="#FFFFFF" />
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span className="brand-title">ProcessHub</span>
            <span className="brand-tag">Enterprise</span>
          </div>
          <p style={{ fontSize: '0.72rem', color: 'var(--text-muted)', lineHeight: 1 }}>
            Intelligent Workflow & Resolution Platform
          </p>
        </div>
      </div>

      {/* Main Navigation Tabs */}
      <nav className="nav-tabs">
        <button 
          className={`nav-tab-btn ${activeTab === 'reporter' ? 'active' : ''}`}
          onClick={() => setActiveTab('reporter')}
          id="nav-tab-reporter"
        >
          My Reports
        </button>
        <button 
          className={`nav-tab-btn ${activeTab === 'supervisor' ? 'active' : ''}`}
          onClick={() => setActiveTab('supervisor')}
          id="nav-tab-supervisor"
        >
          Supervisor Queue
        </button>
        <button 
          className={`nav-tab-btn ${activeTab === 'technician' ? 'active' : ''}`}
          onClick={() => setActiveTab('technician')}
          id="nav-tab-technician"
        >
          Technician Workboard
        </button>
        <button 
          className={`nav-tab-btn ${activeTab === 'finance' ? 'active' : ''}`}
          onClick={() => setActiveTab('finance')}
          id="nav-tab-finance"
        >
          Finance Queue
        </button>
        <button 
          className={`nav-tab-btn ${activeTab === 'verification' ? 'active' : ''}`}
          onClick={() => setActiveTab('verification')}
          id="nav-tab-verification"
        >
          Verification
        </button>
        <button 
          className={`nav-tab-btn ${activeTab === 'analytics' ? 'active' : ''}`}
          onClick={() => setActiveTab('analytics')}
          id="nav-tab-analytics"
        >
          Analytics & SLA
        </button>
      </nav>

      {/* Header Actions */}
      <div className="header-actions">
        {/* Primary Report CTA */}
        <button 
          className="btn-primary" 
          onClick={onOpenReportModal}
          id="btn-report-issue"
        >
          <PlusCircle size={17} />
          <span>Report Problem</span>
        </button>

        {/* Notification Bell */}
        <div style={{ position: 'relative' }}>
          <button 
            style={{ 
              position: 'relative', 
              background: 'var(--bg-primary)', 
              border: '1px solid var(--border-subtle)', 
              borderRadius: 'var(--radius-md)', 
              padding: '8px 10px',
              color: 'var(--text-secondary)'
            }}
            onClick={() => setShowNotifs(!showNotifs)}
            id="btn-notification-bell"
            title="Notifications"
          >
            <Bell size={18} />
            {unreadCount > 0 && (
              <span style={{
                position: 'absolute',
                top: '-4px',
                right: '-4px',
                background: 'var(--rose-500)',
                color: '#FFF',
                borderRadius: 'var(--radius-full)',
                fontSize: '0.68rem',
                fontWeight: 700,
                width: '18px',
                height: '18px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 0 6px var(--rose-glow)'
              }}>
                {unreadCount}
              </span>
            )}
          </button>

          {/* Notifications Dropdown Drawer */}
          {showNotifs && (
            <div style={{
              position: 'absolute',
              right: 0,
              top: '44px',
              width: '360px',
              background: 'var(--bg-primary)',
              border: '1px solid var(--border-medium)',
              borderRadius: 'var(--radius-lg)',
              boxShadow: 'var(--shadow-lg)',
              zIndex: 60,
              overflow: 'hidden'
            }}>
              <div style={{
                padding: '12px 16px',
                borderBottom: '1px solid var(--border-subtle)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                background: 'rgba(30, 41, 59, 0.4)'
              }}>
                <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>Persistent Notifications</span>
                {unreadCount > 0 && (
                  <button 
                    style={{ background: 'transparent', color: 'var(--indigo-500)', fontSize: '0.78rem', fontWeight: 600 }}
                    onClick={onMarkAllRead}
                  >
                    Mark all read
                  </button>
                )}
              </div>

              <div style={{ maxHeight: '340px', overflowY: 'auto' }}>
                {notifications.length === 0 ? (
                  <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    No notifications
                  </div>
                ) : (
                  notifications.map(notif => (
                    <div 
                      key={notif.id}
                      onClick={() => onMarkNotificationRead(notif.id)}
                      style={{
                        padding: '12px 16px',
                        borderBottom: '1px solid var(--border-subtle)',
                        background: notif.read ? 'transparent' : 'rgba(99, 102, 241, 0.06)',
                        cursor: 'pointer',
                        transition: 'background 0.15s ease'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '4px' }}>
                        <span style={{ fontWeight: 600, fontSize: '0.84rem', color: notif.read ? 'var(--text-secondary)' : '#FFF' }}>
                          {notif.title}
                        </span>
                        <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                          {notif.referenceCode}
                        </span>
                      </div>
                      <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', lineHeight: 1.4 }}>
                        {notif.message}
                      </p>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* Role Switcher Pill */}
        <div style={{ position: 'relative' }}>
          <button 
            className="role-switcher"
            onClick={() => setShowRoleMenu(!showRoleMenu)}
            id="btn-role-switcher"
          >
            <UserCheck size={15} color="var(--indigo-500)" />
            <span className={`role-tag-pill ${currentRole}`}>
              {roles.find(r => r.key === currentRole)?.roleName || currentRole}
            </span>
            <ChevronDown size={14} color="var(--text-muted)" />
          </button>

          {showRoleMenu && (
            <div style={{
              position: 'absolute',
              right: 0,
              top: '42px',
              width: '230px',
              background: 'var(--bg-primary)',
              border: '1px solid var(--border-medium)',
              borderRadius: 'var(--radius-lg)',
              boxShadow: 'var(--shadow-lg)',
              zIndex: 60,
              padding: '6px',
            }}>
              <div style={{ padding: '6px 10px', fontSize: '0.72rem', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>
                Simulate Operational Role:
              </div>
              {roles.map(r => (
                <button
                  key={r.key}
                  onClick={() => {
                    setCurrentRole(r.key);
                    setShowRoleMenu(false);
                  }}
                  style={{
                    width: '100%',
                    textAlign: 'left',
                    padding: '8px 12px',
                    borderRadius: 'var(--radius-md)',
                    background: currentRole === r.key ? 'var(--bg-surface-elevated)' : 'transparent',
                    color: currentRole === r.key ? '#FFF' : 'var(--text-secondary)',
                    fontSize: '0.85rem',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between'
                  }}
                >
                  <span>{r.label}</span>
                  {currentRole === r.key && <Check size={14} color="var(--indigo-500)" />}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
