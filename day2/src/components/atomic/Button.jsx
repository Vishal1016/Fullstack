import React from 'react';
import './Button.css';

const Button = ({
  children,
  onClick,
  type = 'button',
  variant = 'primary',
  size = 'md',
  disabled = false,
  loading = false,
  icon: Icon,
  className = '',
  ...props
}) => {
  return (
    <button
      type={type}
      className={`btn btn-${variant} btn-${size} ${loading ? 'btn-loading' : ''} ${className}`}
      onClick={onClick}
      disabled={disabled || loading}
      {...props}
    >
      {loading && <span className="btn-spinner"></span>}
      {!loading && Icon && <Icon className="btn-icon" size={18} />}
      <span className="btn-content">{children}</span>
    </button>
  );
};

export default Button;
