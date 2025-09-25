import React from 'react';

const isSameDay = (a, b) => (
  a.getFullYear() === b.getFullYear() &&
  a.getMonth() === b.getMonth() &&
  a.getDate() === b.getDate()
);

const startOfMonth = (date) => new Date(date.getFullYear(), date.getMonth(), 1);
const endOfMonth = (date) => new Date(date.getFullYear(), date.getMonth() + 1, 0);

const addDays = (date, days) => {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
};

const toYmd = (date) => {
  // Format as local YYYY-MM-DD to avoid UTC shifting to previous/next day
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
};

/**
 * WeekPicker
 * - Friday-only selection, auto-sets end (Thursday)
 * - Past days hidden (not shown) relative to today
 * - Highlights today
 * - Subtle 3D hover/press animations
 */
const WeekPicker = ({
  valueStart,
  valueEnd,
  onChange,
  // Filter mode: allow selecting any day (returns single date)
  selectMode = 'week', // 'week' | 'any'
  onChangeDate,
  minDate,
  compact = false,
  className = '',
  popover = true
}) => {
  const today = React.useMemo(() => {
    const t = new Date();
    t.setHours(0,0,0,0);
    return t;
  }, []);

  const effectiveMin = React.useMemo(() => {
    if (!minDate) return today;
    const md = new Date(minDate);
    md.setHours(0,0,0,0);
    return md;
  }, [minDate, today]);

  const [monthCursor, setMonthCursor] = React.useState(() => startOfMonth(today));
  const [open, setOpen] = React.useState(!popover);
  const panelRef = React.useRef(null);
  const [notice, setNotice] = React.useState('');
  const noticeTimeoutRef = React.useRef(null);

  React.useEffect(() => {
    if (!open) return;
    const handleKey = (e) => {
      if (e.key === 'Escape') setOpen(false);
    };
    const handleClickOutside = (e) => {
      if (panelRef.current && !panelRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    window.addEventListener('keydown', handleKey);
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      window.removeEventListener('keydown', handleKey);
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [open]);

  const goPrevMonth = () => {
    const prev = new Date(monthCursor.getFullYear(), monthCursor.getMonth() - 1, 1);
    setMonthCursor(prev);
  };
  const goNextMonth = () => {
    const next = new Date(monthCursor.getFullYear(), monthCursor.getMonth() + 1, 1);
    setMonthCursor(next);
  };

  const monthStart = startOfMonth(monthCursor);
  const monthEnd = endOfMonth(monthCursor);

  const startWeekday = monthStart.getDay(); // 0 Sun .. 6 Sat
  const daysInMonth = monthEnd.getDate();

  const weeks = [];
  let currentWeek = new Array((startWeekday + 7) % 7).fill(null);
  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(monthCursor.getFullYear(), monthCursor.getMonth(), day);
    const weekday = date.getDay();
    currentWeek.push(date);
    if (weekday === 6 || day === daysInMonth) { // Saturday ends the week
      // Pad end of week
      while (currentWeek.length < 7) currentWeek.push(null);
      weeks.push(currentWeek);
      currentWeek = [];
    }
  }

  const handleSelect = (date) => {
    if (!date) return;
    const day = date.getDay();
    if (date < effectiveMin) return;

    // Any-day selection mode (filter)
    if (selectMode === 'any') {
      onChangeDate?.(toYmd(date));
      setNotice('');
      if (popover) setOpen(false);
      return;
    }

    // Week mode (Friday-only)
    let startDate = date;
    if (day !== 5) {
      const diffToFriday = (5 - day + 7) % 7;
      const snapped = addDays(date, diffToFriday);
      startDate = snapped;
      const msg = `Only Fridays can be selected. Snapped to ${toYmd(snapped)}.`;
      setNotice(msg);
      if (noticeTimeoutRef.current) clearTimeout(noticeTimeoutRef.current);
      noticeTimeoutRef.current = setTimeout(() => setNotice(''), 2200);
    } else {
      setNotice('');
    }
    const end = addDays(startDate, 6); // Thursday
    onChange?.({ start: toYmd(startDate), end: toYmd(end) });
    if (popover) setOpen(false);
  };

  const selectedStart = valueStart ? new Date(valueStart) : null;

  const monthYearLabel = monthCursor.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });

  // Prevent navigating to months entirely before minDate by disabling prev if prev month end < minDate
  const prevDisabled = (() => {
    const prevEnd = endOfMonth(new Date(monthCursor.getFullYear(), monthCursor.getMonth() - 1, 1));
    return prevEnd < effectiveMin;
  })();

  const cellBase = compact ? 'h-8 w-8 text-sm' : 'h-10 w-10';

  const CalendarGrid = (
    <div className="select-none">
      <div className="flex items-center justify-between mb-3">
        <button
          type="button"
          onClick={goPrevMonth}
          disabled={prevDisabled}
          className={`px-2 py-1 rounded-md border border-gray-200 text-gray-600 hover:bg-gray-50 transition ${prevDisabled ? 'opacity-40 cursor-not-allowed' : ''}`}
        >
          ◀
        </button>
        <div className="text-gray-900 font-semibold">{monthYearLabel}</div>
        <button
          type="button"
          onClick={goNextMonth}
          className="px-2 py-1 rounded-md border border-gray-200 text-gray-600 hover:bg-gray-50 transition"
        >
          ▶
        </button>
      </div>

      <div className="grid grid-cols-7 gap-1 mb-1 text-xs text-gray-500">
        <div className="text-center">Sun</div>
        <div className="text-center">Mon</div>
        <div className="text-center">Tue</div>
        <div className="text-center">Wed</div>
        <div className="text-center">Thu</div>
        <div className="text-center font-semibold text-blue-600">Fri</div>
        <div className="text-center">Sat</div>
      </div>

      <div className="grid grid-cols-7 gap-1">
        {weeks.map((week, wi) => (
          <React.Fragment key={wi}>
            {week.map((date, di) => {
              if (date === null) {
                return <div key={`${wi}-${di}`} className={`opacity-0 ${cellBase}`}></div>;
              }
              const isPast = date < today;
              const isFriday = date.getDay() === 5;
              const isSelected = selectedStart && isSameDay(date, selectedStart);
              const isToday = isSameDay(date, today);
              const disabled = isPast && selectMode !== 'any';

              return (
                <button
                  key={`${wi}-${di}`}
                  type="button"
                  onClick={() => handleSelect(date)}
                  disabled={disabled}
                  className={`relative ${cellBase} rounded-lg flex items-center justify-center transition transform [transform-style:preserve-3d]
                    ${isSelected ? 'bg-blue-600 text-white shadow-lg' : 'bg-white text-gray-900 border border-gray-200'}
                    ${disabled ? (isPast ? 'opacity-40 grayscale cursor-not-allowed' : 'cursor-not-allowed') : 'hover:-translate-y-0.5 hover:shadow-md active:translate-y-0 active:shadow'}
                    ${isFriday && !disabled && !isSelected ? 'hover:border-blue-400' : ''}
                  `}
                  title={disabled ? 'Past days cannot be selected' : (selectMode === 'any' ? 'Select date' : (isFriday ? 'Select Friday to set week' : 'Clicking will snap to Friday'))}
                >
                  <span className="z-[1]">{date.getDate()}</span>
                  {isToday && (
                    <span className="absolute inset-0 rounded-lg ring-2 ring-blue-500 ring-offset-2 pointer-events-none"></span>
                  )}
                </button>
              );
            })}
          </React.Fragment>
        ))}
      </div>

      <div className="mt-3 text-xs text-gray-600 space-y-1">
        {selectMode === 'week' && valueStart && valueEnd && (
          <div>
            Selected week: <span className="font-medium text-gray-900">{valueStart}</span> to <span className="font-medium text-gray-900">{valueEnd}</span>
          </div>
        )}
        <div className="text-[11px] text-gray-500">
          {selectMode === 'any'
            ? 'Choose any date to search your reports.'
            : 'Only Fridays are selectable; week ends next Thursday. Previous days are slightly dimmed and cannot be chosen. Today is highlighted in blue.'}
        </div>
      </div>
    </div>
  );

  if (!popover) {
    return (
      <div className={className}>
        {CalendarGrid}
      </div>
    );
  }

  return (
    <div className={`relative ${className}`}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="w-full md:w-auto px-4 py-2 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 text-left flex items-center justify-between gap-3"
      >
        <span className="text-sm text-gray-700">
          {selectMode === 'any'
            ? (valueStart ? `Date: ${valueStart}` : 'Select date')
            : (valueStart ? `Friday: ${valueStart}` : 'Select week (Friday start)')}
        </span>
        <span className="text-gray-500">▾</span>
      </button>

      {notice && (
        <div className="absolute z-50 mt-2 right-0 w-max max-w-[80vw] bg-blue-50 text-blue-800 border border-blue-200 rounded-md px-3 py-2 text-xs shadow">
          {notice}
        </div>
      )}

      {open && (
        <div
          ref={panelRef}
          className="absolute z-50 mt-2 w-80 max-w-[90vw] bg-white/95 backdrop-blur border border-gray-200 shadow-xl rounded-xl p-3 origin-top-right animate-[fadeIn_120ms_ease-out]"
          style={{ right: 0 }}
        >
          <div className="transition-transform duration-150 ease-out">
            {CalendarGrid}
          </div>
        </div>
      )}
    </div>
  );
};

export default WeekPicker;


