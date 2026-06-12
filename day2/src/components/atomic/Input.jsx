import React from 'react';
import './Input.css';

const Input = ({
  label,
  id,
  type = 'text',
  placeholder,
  value,
  onChange,
  error,
  helperText,
  icon: Icon,
  className = '',
  required = false,
  ...props
}) => {
  return (
    <div className={`input-container ${error ? 'input-has-error' : ''} ${className}`}>
      {label && (
        <label htmlFor={id} className="input-label">
          {label} {required && <span className="input-required">*</span>}
        </label>
      )}
      <div className="input-wrapper">
        {Icon && <Icon className="input-icon-left" size={18} />}
        <input
          id={id}
          type={type}
          placeholder={placeholder}
          value={value}
          onChange={onChange}
          className={`input-field ${Icon ? 'input-field-with-icon' : ''}`}
          required={required}
          {...props}
        />
      </div>
      {error && <span className="input-error-msg">{error}</span>}
      {!error && helperText && <span className="input-helper-msg">{helperText}</span>}
    </div>
  );
};

export default Input;
