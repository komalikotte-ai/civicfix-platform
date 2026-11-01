import React, { useRef, useState } from 'react';
import Button from '../../../components/common/Button';

const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
const ALLOWED_TYPES = [
  'image/jpeg',
  'image/png',
  'image/webp',
  'image/gif',
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
];

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${(bytes / Math.pow(k, i)).toFixed(1)} ${sizes[i]}`;
};

export const AttachmentUpload = ({ attachments = [], onChange, maxFiles = 5 }) => {
  const fileInputRef = useRef(null);
  const [dragOver, setDragOver] = useState(false);
  const [error, setError] = useState('');

  const processFiles = (fileList) => {
    setError('');
    const newItems = Array.from(fileList);

    if (attachments.length + newItems.length > maxFiles) {
      setError(`You can attach a maximum of ${maxFiles} files.`);
      return;
    }

    newItems.forEach((file) => {
      if (file.size > MAX_FILE_SIZE) {
        setError(`File "${file.name}" exceeds the 10 MB limit.`);
        return;
      }

      if (!ALLOWED_TYPES.includes(file.type)) {
        setError(`File "${file.name}" has an unsupported format. Allowed: Images, PDF, DOC.`);
        return;
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        const item = {
          fileName: file.name,
          fileUrl: e.target.result, // Data URL
          fileType: file.type,
          fileSize: file.size,
          previewUrl: file.type.startsWith('image/') ? e.target.result : null,
        };
        onChange((prev) => [...prev, item]);
      };
      reader.readAsDataURL(file);
    });
  };

  const handleFileDrop = (e) => {
    e.preventDefault();
    setDragOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      processFiles(e.dataTransfer.files);
    }
  };

  const handleFileInputChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      processFiles(e.target.files);
    }
  };

  const handleRemove = (index) => {
    onChange((prev) => prev.filter((_, i) => i !== index));
  };

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center justify-between">
        <label className="text-xs font-semibold text-slate-700">
          Photographic Evidence & Documents (Optional)
        </label>
        <span className="text-[11px] text-slate-500">
          {attachments.length} of {maxFiles} files attached
        </span>
      </div>

      {error && (
        <div className="p-2.5 rounded-lg bg-red-50 border border-red-200 text-red-700 text-xs flex items-center justify-between">
          <span>{error}</span>
          <button type="button" onClick={() => setError('')} className="font-bold ml-2">
            ✕
          </button>
        </div>
      )}

      {/* Drag & Drop Zone */}
      {attachments.length < maxFiles && (
        <div
          onDragOver={(e) => {
            e.preventDefault();
            setDragOver(true);
          }}
          onDragLeave={() => setDragOver(false)}
          onDrop={handleFileDrop}
          onClick={() => fileInputRef.current?.click()}
          className={`border-2 border-dashed rounded-xl p-5 text-center cursor-pointer transition-colors ${
            dragOver
              ? 'border-blue-500 bg-blue-50/50'
              : 'border-slate-300 hover:border-slate-400 bg-slate-50/50'
          }`}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ' ') {
              e.preventDefault();
              fileInputRef.current?.click();
            }
          }}
          aria-label="Upload attachments dropzone"
        >
          <input
            ref={fileInputRef}
            type="file"
            multiple
            accept="image/*,application/pdf,.doc,.docx"
            onChange={handleFileInputChange}
            className="hidden"
          />
          <div className="flex flex-col items-center gap-1.5 pointer-events-none">
            <span className="text-2xl" aria-hidden="true">📷</span>
            <div className="text-xs font-semibold text-slate-800">
              Drag and drop photos or documents, or <span className="text-blue-600 underline">browse</span>
            </div>
            <div className="text-[11px] text-slate-500">
              Supports JPEG, PNG, WebP, PDF up to 10 MB each
            </div>
          </div>
        </div>
      )}

      {/* Attachment List / Previews */}
      {attachments.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-1">
          {attachments.map((file, idx) => (
            <div
              key={idx}
              className="flex items-center gap-3 p-2.5 rounded-lg border border-slate-200 bg-white shadow-xs relative"
            >
              {file.previewUrl ? (
                <img
                  src={file.previewUrl}
                  alt={file.fileName}
                  className="w-12 h-12 rounded object-cover border border-slate-100 flex-shrink-0"
                />
              ) : (
                <div className="w-12 h-12 rounded bg-blue-50 border border-blue-100 text-blue-600 flex items-center justify-center text-xs font-bold flex-shrink-0">
                  📄
                </div>
              )}
              <div className="flex-1 min-w-0 pr-6">
                <div className="text-xs font-medium text-slate-800 truncate" title={file.fileName}>
                  {file.fileName}
                </div>
                <div className="text-[11px] text-slate-500 mt-0.5">
                  {formatFileSize(file.fileSize)} • {file.fileType.split('/')[1]?.toUpperCase()}
                </div>
              </div>
              <button
                type="button"
                onClick={() => handleRemove(idx)}
                className="absolute top-2 right-2 text-slate-400 hover:text-red-600 p-1 rounded-md"
                title="Remove attachment"
                aria-label={`Remove ${file.fileName}`}
              >
                ✕
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AttachmentUpload;
