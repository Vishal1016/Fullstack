import React from 'react';
import './StatCard.css';

const StatCard = ({
  title,
  value,
  icon: Icon,
  subtext,
  trend,
  trendType = 'neutral' // 'up' | 'down' | 'neutral'
}) => {
  return (
    <div className="stat-card glass-panel animate-fade-in">
      <div className="stat-header">
        <div className="stat-info">
          <span className="stat-title">{title}</span>
          <h3 className="stat-value">{value}</h3>
        </div>
        {Icon && (
          <div className="stat-icon-wrapper">
            <Icon size={22} className="stat-icon" />
          </div>
        )}
      </div>
      {subtext && (
        <div className="stat-footer">
          {trend && (
            <span className={`stat-trend trend-${trendType}`}>
              {trendType === 'up' ? '▲' : trendType === 'down' ? '▼' : '•'} {trend}
            </span>
          )}
          <span className="stat-subtext">{subtext}</span>
        </div>
      )}
    </div>
  );
};

export default StatCard;
