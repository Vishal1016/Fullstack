import React from 'react';
import './SeatLayout.css';

const SeatLayout = ({
  occupiedSeats = [],
  selectedSeats = [],
  onSeatClick
}) => {
  const rows = ['A', 'B', 'C', 'D', 'E', 'F'];
  const cols = [1, 2, 3, 4, 5, 6, 7, 8];

  const handleSeatClick = (seatId) => {
    if (occupiedSeats.includes(seatId)) return;
    onSeatClick(seatId);
  };

  return (
    <div className="seat-booking-panel">
      {/* Screen representation */}
      <div className="screen-container">
        <div className="screen-curve"></div>
        <span className="screen-label">CINEMA SCREEN</span>
      </div>

      {/* Grid of seats */}
      <div className="seats-grid">
        {rows.map((row) => (
          <div key={row} className="seat-row">
            <span className="row-label">{row}</span>
            <div className="row-seats">
              {cols.map((col) => {
                const seatId = `${row}${col}`;
                const isOccupied = occupiedSeats.includes(seatId);
                const isSelected = selectedSeats.includes(seatId);
                
                let seatClass = 'seat-available';
                if (isOccupied) seatClass = 'seat-occupied';
                else if (isSelected) seatClass = 'seat-selected';

                return (
                  <button
                    key={seatId}
                    className={`seat ${seatClass}`}
                    onClick={() => handleSeatClick(seatId)}
                    disabled={isOccupied}
                    title={isOccupied ? `Seat ${seatId} (Occupied)` : `Seat ${seatId}`}
                  >
                    {col}
                  </button>
                );
              })}
            </div>
            <span className="row-label">{row}</span>
          </div>
        ))}
      </div>

      {/* Legend key */}
      <div className="seat-legend">
        <div className="legend-item">
          <div className="legend-box seat-available"></div>
          <span>Available</span>
        </div>
        <div className="legend-item">
          <div className="legend-box seat-selected"></div>
          <span>Selected</span>
        </div>
        <div className="legend-item">
          <div className="legend-box seat-occupied"></div>
          <span>Occupied</span>
        </div>
      </div>
    </div>
  );
};

export default SeatLayout;
