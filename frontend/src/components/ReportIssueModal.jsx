import React, { useState } from 'react';
import { 
  X, 
  AlertCircle, 
  Building2, 
  MapPin, 
  Cpu, 
  Send, 
  Upload, 
  ShieldCheck 
} from 'lucide-react';

export default function ReportIssueModal({ 
  isOpen, 
  onClose, 
  departments, 
  categories, 
  onSubmit 
}) {
  if (!isOpen) return null;

  const [departmentId, setDepartmentId] = useState(departments[0]?.id || '');
  const [categoryId, setCategoryId] = useState(categories[0]?.id || '');
  const [subtype, setSubtype] = useState('');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [locationBuilding, setLocationBuilding] = useState('Science Complex A');
  const [locationRoom, setLocationRoom] = useState('Room 204');
  const [assetId, setAssetId] = useState('');
  const [priority, setPriority] = useState('HIGH');
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // Filter categories by selected department
  const filteredCategories = categories.filter(c => !departmentId || c.departmentId === departmentId);
  const selectedCategory = categories.find(c => c.id === categoryId);

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrorMsg('');

    if (!title || title.trim().length < 5) {
      setErrorMsg('Title must be at least 5 characters long.');
      return;
    }

    if (!description || description.trim().length < 10) {
      setErrorMsg('Description must be at least 10 characters long.');
      return;
    }

    if (!locationBuilding.trim() || !locationRoom.trim()) {
      setErrorMsg('Building and room location are required.');
      return;
    }

    if (selectedCategory?.requiresAsset && (!assetId || !assetId.trim())) {
      setErrorMsg(`Asset tag is mandatory for category: ${selectedCategory.name}`);
      return;
    }

    setSubmitting(true);
    onSubmit({
      departmentId,
      categoryId,
      subtype: subtype || 'GENERAL',
      title: title.trim(),
      description: description.trim(),
      locationBuilding: locationBuilding.trim(),
      locationRoom: locationRoom.trim(),
      assetId: assetId.trim() || null,
      priority,
    });
    setSubmitting(false);
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-dialog" onClick={e => e.stopPropagation()}>
        <form onSubmit={handleSubmit}>
          {/* Header */}
          <div className="modal-header">
            <span style={{ fontWeight: 700, fontSize: '1.1rem' }}>
              Submit New Problem Report
            </span>
            <button 
              type="button" 
              onClick={onClose} 
              style={{ background: 'transparent', color: 'var(--text-muted)' }}
            >
              <X size={20} />
            </button>
          </div>

          {/* Body */}
          <div className="modal-body">
            {errorMsg && (
              <div className="blocker-alert" style={{ background: 'rgba(239, 68, 68, 0.12)', borderColor: 'rgba(239, 68, 68, 0.3)', color: '#FCA5A5', marginBottom: '16px' }}>
                <AlertCircle size={16} />
                <span>{errorMsg}</span>
              </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '14px' }}>
              <div>
                <label style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                  Target Department:
                </label>
                <select 
                  value={departmentId}
                  onChange={e => {
                    setDepartmentId(e.target.value);
                    const matchingCat = categories.find(c => c.departmentId === e.target.value);
                    if (matchingCat) setCategoryId(matchingCat.id);
                  }}
                  id="select-report-dept"
                >
                  {departments.map(d => (
                    <option key={d.id} value={d.id}>{d.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                  Service Category:
                </label>
                <select 
                  value={categoryId}
                  onChange={e => setCategoryId(e.target.value)}
                  id="select-report-category"
                >
                  {filteredCategories.map(c => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
            </div>

            <div style={{ marginBottom: '14px' }}>
              <label style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Issue Title (concise summary):
              </label>
              <input 
                type="text" 
                placeholder="e.g. Overhead pipe leak dripping on electrical conduit"
                value={title}
                onChange={e => setTitle(e.target.value)}
                id="input-report-title"
              />
            </div>

            <div style={{ marginBottom: '14px' }}>
              <label style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Observed Symptoms & Technical Details:
              </label>
              <textarea 
                rows={3} 
                placeholder="Describe the problem, severity, and any immediate hazard..."
                value={description}
                onChange={e => setDescription(e.target.value)}
                id="input-report-desc"
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '14px' }}>
              <div>
                <label style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                  Building / Zone:
                </label>
                <input 
                  type="text" 
                  value={locationBuilding}
                  onChange={e => setLocationBuilding(e.target.value)}
                  id="input-report-building"
                />
              </div>

              <div>
                <label style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                  Room / Specific Location:
                </label>
                <input 
                  type="text" 
                  value={locationRoom}
                  onChange={e => setLocationRoom(e.target.value)}
                  id="input-report-room"
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px', marginBottom: '14px' }}>
              <div>
                <label style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                  Asset Tag / Equipment ID {selectedCategory?.requiresAsset && <span style={{ color: '#F87171' }}>*</span>}:
                </label>
                <input 
                  type="text" 
                  placeholder={selectedCategory?.requiresAsset ? "Mandatory for this category" : "Optional (e.g. PUMP-409)"}
                  value={assetId}
                  onChange={e => setAssetId(e.target.value)}
                  id="input-report-asset"
                />
              </div>

              <div>
                <label style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                  Reported Severity / Priority:
                </label>
                <select 
                  value={priority}
                  onChange={e => setPriority(e.target.value)}
                  id="select-report-priority"
                >
                  <option value="LOW">Low (Routine maintenance)</option>
                  <option value="MEDIUM">Medium (Standard operation)</option>
                  <option value="HIGH">High (Urgent facility impact)</option>
                  <option value="CRITICAL">Critical (Immediate safety / shutdown hazard)</option>
                </select>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="modal-footer">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button 
              type="submit" 
              className="btn-primary" 
              disabled={submitting}
              id="btn-submit-report"
            >
              <Send size={15} />
              <span>{submitting ? 'Generating Reference...' : 'Submit Report'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
