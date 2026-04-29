import React, { useState, useEffect } from 'react';
import { taskService } from '../services/api';
import '../styles/CalendarPage.css';

const CalendarPage = () => {
    const [tasks, setTasks] = useState([]);
    const [currentMonth, setCurrentMonth] = useState(() => {
        const now = new Date();
        return new Date(now.getFullYear(), now.getMonth(), 1);
    });

    useEffect(() => {
        taskService.getAll().then(data => setTasks(data));
    }, []);

    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const firstDay = (new Date(year, month, 1).getDay() + 6) % 7; // Monday-based
    const totalCells = Math.ceil((firstDay + daysInMonth) / 7) * 7;
    const monthPrefix = `${year}-${String(month + 1).padStart(2, '0')}`;
    const today = new Date();
    const todayDateString = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
    const cells = Array.from({ length: totalCells }, (_, index) => {
        const day = index - firstDay + 1;
        return day > 0 && day <= daysInMonth ? day : null;
    });

    const goToPreviousMonth = () => {
        setCurrentMonth(prev => new Date(prev.getFullYear(), prev.getMonth() - 1, 1));
    };

    const goToNextMonth = () => {
        setCurrentMonth(prev => new Date(prev.getFullYear(), prev.getMonth() + 1, 1));
    };

    const goToCurrentMonth = () => {
        const now = new Date();
        setCurrentMonth(new Date(now.getFullYear(), now.getMonth(), 1));
    };

                    <button type="button" className="btn btn-sm btn-edit" onClick={goToPreviousMonth}>
        <div className="page">
                    <button type="button" className="btn btn-sm" onClick={goToCurrentMonth}>
                        Сегодня
                    </button>
                    <button type="button" className="btn btn-sm btn-edit" onClick={goToNextMonth}>
                        ←
                    </button>
                    <h2>{currentMonth.toLocaleString('ru', { month: 'long', year: 'numeric' })}</h2>
                    <button type="button" className="btn btn-sm" onClick={goToNextMonth}>
                        →
                    </button>
                </div>
                <h2>{currentMonth.toLocaleString('ru', { month: 'long', year: 'numeric' })}</h2>
            </div>

            <div className="calendar-grid">
                {['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'].map(d => (
                    <div key={d} className="weekday-label">
                        {d}
                    </div>
                ))}

                {cells.map((day, index) => {
                    const dateStr = day ? `${monthPrefix}-${day.toString().padStart(2, '0')}` : null;
                    const dayTasks = dateStr
                        ? tasks.filter(t => t.dueDate?.startsWith(dateStr))
                        : [];
                    const isToday = dateStr === todayDateString;

                    return (
                                {dayTasks.map(t => (
                            key={`${index}-${day ?? 'empty'}`}
                            className={`calendar-cell ${day ? '' : 'calendar-cell--empty'} ${isToday ? 'calendar-cell--today' : ''}`}
                        >
                            <div className="cell-tasks">
                                {dayTasks.slice(0, 3).map(t => (
                                    <div key={t.id} className={`task-badge ${t.completed ? 'done' : ''}`}>
                                        {t.title}
                                    </div>
                                ))}
                                {dayTasks.length > 3 && <div className="task-badge more">+{dayTasks.length - 3}</div>}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default CalendarPage;