// Modern UI JavaScript for Enhanced User Experience
// Inspired by global modern web applications

document.addEventListener('DOMContentLoaded', function() {
    
    // ===== Loading Animation =====
    function showLoadingSpinner() {
        const spinner = document.createElement('div');
        spinner.id = 'loading-spinner';
        spinner.innerHTML = `
            <div class="spinner-overlay">
                <div class="spinner">
                    <div class="double-bounce1"></div>
                    <div class="double-bounce2"></div>
                </div>
                <p class="loading-text">処理中...</p>
            </div>
        `;
        document.body.appendChild(spinner);
    }
    
    function hideLoadingSpinner() {
        const spinner = document.getElementById('loading-spinner');
        if (spinner) {
            spinner.remove();
        }
    }
    
    // ===== Form Enhancements =====
    
    // Auto-save draft functionality for forms
    function setupAutoSave() {
        const forms = document.querySelectorAll('form[action="reservation"]');
        forms.forEach(form => {
            const inputs = form.querySelectorAll('input[type="text"], input[type="datetime-local"]');
            inputs.forEach(input => {
                // Load saved data
                const savedValue = localStorage.getItem(`draft_${input.name}`);
                if (savedValue && !input.value) {
                    input.value = savedValue;
                    input.classList.add('auto-filled');
                }
                
                // Save on input
                input.addEventListener('input', () => {
                    localStorage.setItem(`draft_${input.name}`, input.value);
                });
                
                // Clear draft on successful submit
                form.addEventListener('submit', () => {
                    setTimeout(() => {
                        if (!document.querySelector('.error-message') || 
                            document.querySelector('.error-message').textContent.trim() === '') {
                            localStorage.removeItem(`draft_${input.name}`);
                        }
                    }, 100);
                });
            });
        });
    }
    
    // Enhanced form validation with real-time feedback
    function setupFormValidation() {
        const inputs = document.querySelectorAll('input[required]');
        inputs.forEach(input => {
            input.addEventListener('blur', function() {
                validateField(this);
            });
            
            input.addEventListener('input', function() {
                if (this.classList.contains('invalid')) {
                    validateField(this);
                }
            });
        });
    }
    
    function validateField(field) {
        const isValid = field.checkValidity();
        const parent = field.closest('p') || field.parentElement;
        
        // Remove existing validation messages
        const existingMessage = parent.querySelector('.validation-message');
        if (existingMessage) {
            existingMessage.remove();
        }
        
        field.classList.remove('invalid', 'valid');
        
        if (!isValid) {
            field.classList.add('invalid');
            const message = document.createElement('span');
            message.className = 'validation-message error';
            message.textContent = getValidationMessage(field);
            parent.appendChild(message);
        } else if (field.value.trim() !== '') {
            field.classList.add('valid');
        }
    }
    
    function getValidationMessage(field) {
        if (field.type === 'datetime-local') {
            const selectedDate = new Date(field.value);
            const now = new Date();
            if (selectedDate < now) {
                return '過去の日時は選択できません';
            }
        }
        return field.validationMessage || 'この項目は必須です';
    }
    
    // ===== Table Enhancements =====
    
    // Row highlighting and smooth interactions
    function enhanceTable() {
        const tables = document.querySelectorAll('table');
        tables.forEach(table => {
            const rows = table.querySelectorAll('tbody tr');
            
            rows.forEach(row => {
                // Add hover effect
                row.addEventListener('mouseenter', function() {
                    this.style.transform = 'scale(1.01)';
                    this.style.zIndex = '10';
                });
                
                row.addEventListener('mouseleave', function() {
                    this.style.transform = '';
                    this.style.zIndex = '';
                });
                
                // Add click ripple effect
                row.addEventListener('click', function(e) {
                    if (e.target.tagName !== 'BUTTON' && e.target.tagName !== 'A' && e.target.tagName !== 'INPUT') {
                        createRippleEffect(e, this);
                    }
                });
            });
        });
    }
    
    // ===== Interactive Elements =====
    
    // Ripple effect for buttons and clickable elements
    function createRippleEffect(event, element) {
        const ripple = document.createElement('div');
        const rect = element.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = event.clientX - rect.left - size / 2;
        const y = event.clientY - rect.top - size / 2;
        
        ripple.style.cssText = `
            position: absolute;
            width: ${size}px;
            height: ${size}px;
            left: ${x}px;
            top: ${y}px;
            background: rgba(79, 70, 229, 0.3);
            border-radius: 50%;
            transform: scale(0);
            animation: ripple 0.6s ease-out;
            pointer-events: none;
            z-index: 1000;
        `;
        
        element.style.position = 'relative';
        element.style.overflow = 'hidden';
        element.appendChild(ripple);
        
        setTimeout(() => {
            ripple.remove();
        }, 600);
    }
    
    // Enhanced button interactions
    function setupButtonInteractions() {
        const buttons = document.querySelectorAll('button, .button, input[type="submit"]');
        buttons.forEach(button => {
            button.addEventListener('click', function(e) {
                createRippleEffect(e, this);
                
                // Add loading state for form submissions
                if (this.type === 'submit' || this.tagName === 'BUTTON') {
                    const originalText = this.textContent || this.value;
                    const form = this.closest('form');
                    
                    if (form && !this.classList.contains('no-loading')) {
                        setTimeout(() => {
                            this.style.opacity = '0.7';
                            this.style.pointerEvents = 'none';
                            if (this.tagName === 'INPUT') {
                                this.value = '処理中...';
                            } else {
                                this.textContent = '処理中...';
                            }
                        }, 100);
                    }
                }
            });
        });
    }
    
    // ===== Search and Filter Enhancements =====
    
    // Live search functionality
    function setupLiveSearch() {
        const searchInput = document.querySelector('input[name="search"]');
        if (searchInput) {
            let searchTimeout;
            
            searchInput.addEventListener('input', function() {
                clearTimeout(searchTimeout);
                const query = this.value.trim();
                
                if (query.length >= 2) {
                    searchTimeout = setTimeout(() => {
                        highlightSearchResults(query);
                    }, 300);
                } else {
                    clearHighlights();
                }
            });
        }
    }
    
    function highlightSearchResults(query) {
        const tableRows = document.querySelectorAll('tbody tr');
        const regex = new RegExp(`(${query})`, 'gi');
        
        tableRows.forEach(row => {
            const cells = row.querySelectorAll('td');
            let hasMatch = false;
            
            cells.forEach(cell => {
                if (!cell.querySelector('button, a, form')) {
                    const originalText = cell.dataset.originalText || cell.textContent;
                    cell.dataset.originalText = originalText;
                    
                    if (originalText.toLowerCase().includes(query.toLowerCase())) {
                        hasMatch = true;
                        cell.innerHTML = originalText.replace(regex, '<mark>$1</mark>');
                    } else {
                        cell.textContent = originalText;
                    }
                }
            });
            
            row.style.opacity = hasMatch ? '1' : '0.4';
        });
    }
    
    function clearHighlights() {
        const tableRows = document.querySelectorAll('tbody tr');
        tableRows.forEach(row => {
            row.style.opacity = '1';
            const cells = row.querySelectorAll('td');
            cells.forEach(cell => {
                if (cell.dataset.originalText) {
                    cell.textContent = cell.dataset.originalText;
                }
            });
        });
    }
    
    // ===== Toast Notifications =====
    
    function showToast(message, type = 'info', duration = 4000) {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <div class="toast-content">
                <span class="toast-icon">${getToastIcon(type)}</span>
                <span class="toast-message">${message}</span>
                <button class="toast-close" onclick="this.parentElement.parentElement.remove()">×</button>
            </div>
        `;
        
        const toastContainer = document.getElementById('toast-container') || createToastContainer();
        toastContainer.appendChild(toast);
        
        // Animate in
        setTimeout(() => toast.classList.add('show'), 100);
        
        // Auto remove
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, duration);
    }
    
    function createToastContainer() {
        const container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
        return container;
    }
    
    function getToastIcon(type) {
        const icons = {
            success: '✓',
            error: '✕',
            warning: '⚠',
            info: 'ℹ'
        };
        return icons[type] || icons.info;
    }
    
    // ===== Smooth Transitions =====
    
    // Fast page transition effects
    function setupPageTransitions() {
        const links = document.querySelectorAll('a:not([target="_blank"]):not(.no-transition)');
        links.forEach(link => {
            link.addEventListener('click', function(e) {
                if (this.href && this.href.indexOf('#') === -1) {
                    e.preventDefault();
                    const href = this.href;
                    
                    document.body.style.opacity = '0';
                    document.body.style.transform = 'translateY(10px)';
                    
                    setTimeout(() => {
                        window.location.href = href;
                    }, 100);
                }
            });
        });
    }
    
    // No page load animation - instant display
    function animatePageLoad() {
        // Remove fade in animation for instant display
        // document.body.style.opacity = '0';
        // document.body.style.transform = 'translateY(10px)';
        
        // setTimeout(() => {
        //     document.body.style.transition = 'opacity 0.2s ease, transform 0.2s ease';
        //     document.body.style.opacity = '1';
        //     document.body.style.transform = 'translateY(0)';
        // }, 50);
    }
    
    // ===== Accessibility Enhancements =====
    
    // Keyboard navigation improvements
    function setupKeyboardNavigation() {
        document.addEventListener('keydown', function(e) {
            // Escape key to close modals/overlays
            if (e.key === 'Escape') {
                const spinner = document.getElementById('loading-spinner');
                if (spinner) hideLoadingSpinner();
            }
            
            // Enter key for buttons
            if (e.key === 'Enter' && document.activeElement.classList.contains('button')) {
                document.activeElement.click();
            }
        });
    }
    
    // ===== Error Handling =====
    
    // Convert error messages to toast notifications
    function convertErrorMessagesToToasts() {
        const errorMessages = document.querySelectorAll('.error-message');
        errorMessages.forEach(msg => {
            if (msg.textContent.trim()) {
                showToast(msg.textContent.trim(), 'error');
                msg.style.display = 'none';
            }
        });
        
        const successMessages = document.querySelectorAll('.success-message');
        successMessages.forEach(msg => {
            if (msg.textContent.trim()) {
                showToast(msg.textContent.trim(), 'success');
                msg.style.display = 'none';
            }
        });
    }
    
    // ===== Initialize All Features =====
    
    function initializeModernUI() {
        animatePageLoad();
        setupAutoSave();
        setupFormValidation();
        enhanceTable();
        setupButtonInteractions();
        setupLiveSearch();
        setupPageTransitions();
        setupKeyboardNavigation();
        convertErrorMessagesToToasts();
        
        // Show welcome toast on first load
        if (!sessionStorage.getItem('welcomeShown')) {
            setTimeout(() => {
                showToast('予約システムへようこそ！', 'info');
                sessionStorage.setItem('welcomeShown', 'true');
            }, 1000);
        }
        
        console.log('Modern UI initialized successfully! 🚀');
    }
    
    // Initialize everything
    initializeModernUI();
    
});

// ===== CSS Animations (injected dynamically) =====
const style = document.createElement('style');
style.textContent = `
    @keyframes ripple {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
    
    .auto-filled {
        border-color: var(--info-color) !important;
        background-color: rgba(59, 130, 246, 0.05) !important;
    }
    
    .validation-message {
        display: block;
        margin-top: 0.5rem;
        font-size: 0.875rem;
        font-weight: 500;
    }
    
    .validation-message.error {
        color: var(--danger-color);
    }
    
    input.invalid {
        border-color: var(--danger-color) !important;
        box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1) !important;
    }
    
    input.valid {
        border-color: var(--success-color) !important;
        box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1) !important;
    }
    
    mark {
        background-color: rgba(251, 191, 36, 0.3);
        padding: 0.125rem;
        border-radius: 0.25rem;
    }
    
    #toast-container {
        position: fixed;
        top: 1rem;
        right: 1rem;
        z-index: 10000;
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }
    
    .toast {
        opacity: 0;
        transform: translateX(100%);
        transition: all 0.3s ease;
        max-width: 400px;
    }
    
    .toast.show {
        opacity: 1;
        transform: translateX(0);
    }
    
    .toast-content {
        background: white;
        padding: 1rem;
        border-radius: 0.5rem;
        box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
        border-left: 4px solid;
        display: flex;
        align-items: center;
        gap: 0.75rem;
        position: relative;
    }
    
    .toast-success .toast-content { border-left-color: var(--success-color); }
    .toast-error .toast-content { border-left-color: var(--danger-color); }
    .toast-warning .toast-content { border-left-color: var(--warning-color); }
    .toast-info .toast-content { border-left-color: var(--info-color); }
    
    .toast-icon {
        font-size: 1.25rem;
        font-weight: bold;
    }
    
    .toast-success .toast-icon { color: var(--success-color); }
    .toast-error .toast-icon { color: var(--danger-color); }
    .toast-warning .toast-icon { color: var(--warning-color); }
    .toast-info .toast-icon { color: var(--info-color); }
    
    .toast-close {
        position: absolute;
        top: 0.25rem;
        right: 0.5rem;
        background: none;
        border: none;
        font-size: 1.25rem;
        cursor: pointer;
        opacity: 0.5;
        transition: opacity 0.2s;
    }
    
    .toast-close:hover {
        opacity: 1;
    }
    
    #loading-spinner {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 10000;
    }
    
    .spinner-overlay {
        text-align: center;
        color: white;
    }
    
    .spinner {
        width: 60px;
        height: 60px;
        position: relative;
        margin: 0 auto 1rem;
    }
    
    .double-bounce1, .double-bounce2 {
        width: 100%;
        height: 100%;
        border-radius: 50%;
        background-color: white;
        opacity: 0.6;
        position: absolute;
        top: 0;
        left: 0;
        animation: sk-bounce 2.0s infinite ease-in-out;
    }
    
    .double-bounce2 {
        animation-delay: -1.0s;
    }
    
    @keyframes sk-bounce {
        0%, 100% {
            transform: scale(0);
        }
        50% {
            transform: scale(1);
        }
    }
    
    .loading-text {
        font-size: 1.1rem;
        font-weight: 500;
    }
    
    /* Enhanced responsive design */
    @media (max-width: 768px) {
        #toast-container {
            right: 0.5rem;
            left: 0.5rem;
        }
        
        .toast {
            max-width: none;
        }
    }
`;
document.head.appendChild(style);