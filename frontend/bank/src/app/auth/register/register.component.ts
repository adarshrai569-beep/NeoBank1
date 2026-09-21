import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';

@Component({
    standalone: true,
    selector: 'app-register',
    imports: [CommonModule, FormsModule],
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {

    user = {
        fullName: '',
        mobile: '',
        email: '',
        accountType: 'SAVINGS',
        idType: 'AADHAR',
        idNumber: '',
        address: '',
        password: '',
        confirmPassword: ''
    };

    errors: any = {};
    isSubmitting = false;
    showPasswordHints = false;

    constructor(private auth: AuthService, private router: Router) { }

    // Helper methods for template validation (fixes regex issues)
    hasLowercase(): boolean {
        return /[a-z]/.test(this.user.password);
    }

    hasUppercase(): boolean {
        return /[A-Z]/.test(this.user.password);
    }

    hasDigit(): boolean {
        return /\d/.test(this.user.password);
    }

    hasSpecialChar(): boolean {
        return /[@$!%*?&]/.test(this.user.password);
    }

    hasMinLength(): boolean {
        return this.user.password.length >= 8;
    }

    // Input sanitization methods
    sanitizeMobile(): void {
        this.user.mobile = this.user.mobile.replace(/[^0-9]/g, '');
    }

    sanitizeIdNumber(): void {
        if (this.user.idType === 'AADHAR') {
            this.user.idNumber = this.user.idNumber.replace(/[^0-9]/g, '');
        } else {
            this.user.idNumber = this.user.idNumber.toUpperCase();
        }
    }

    validateField(fieldName: string): void {
        switch(fieldName) {
            case 'fullName':
                this.validateFullName();
                break;
            case 'mobile':
                this.validateMobile();
                break;
            case 'email':
                this.validateEmail();
                break;
            case 'idNumber':
                this.validateIdNumber();
                break;
            case 'address':
                this.validateAddress();
                break;
            case 'password':
                this.validatePassword();
                break;
            case 'confirmPassword':
                this.validateConfirmPassword();
                break;
        }
    }

    validateFullName(): boolean {
        const fullName = this.user.fullName.trim();
        
        if (!fullName) {
            this.errors.fullName = 'Full name is required';
            return false;
        }
        
        if (fullName.length < 2) {
            this.errors.fullName = 'Full name must be at least 2 characters';
            return false;
        }
        
        if (fullName.length > 50) {
            this.errors.fullName = 'Full name cannot exceed 50 characters';
            return false;
        }
        
        if (!/^[a-zA-Z\s.']+$/.test(fullName)) {
            this.errors.fullName = 'Full name can only contain letters, spaces, dots and apostrophes';
            return false;
        }

        delete this.errors.fullName;
        return true;
    }

    validateMobile(): boolean {
        const mobile = this.user.mobile.trim();
        
        if (!mobile) {
            this.errors.mobile = 'Mobile number is required';
            return false;
        }
        
        if (!/^[6-9]\d{9}$/.test(mobile)) {
            this.errors.mobile = 'Enter valid Indian mobile number (10 digits starting with 6-9)';
            return false;
        }

        delete this.errors.mobile;
        return true;
    }

    validateEmail(): boolean {
        const email = this.user.email.trim().toLowerCase();
        
        if (!email) {
            this.errors.email = 'Email address is required';
            return false;
        }
        
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(email)) {
            this.errors.email = 'Please enter a valid email address';
            return false;
        }

        delete this.errors.email;
        return true;
    }

    validateIdNumber(): boolean {
        const idNumber = this.user.idNumber.trim().toUpperCase();
        
        if (!idNumber) {
            this.errors.idNumber = `${this.user.idType} number is required`;
            return false;
        }

        if (this.user.idType === 'AADHAR') {
            if (!/^\d{12}$/.test(idNumber)) {
                this.errors.idNumber = 'Aadhar number must be exactly 12 digits';
                return false;
            }
        } else if (this.user.idType === 'PAN') {
            if (!/^[A-Z]{5}[0-9]{4}[A-Z]{1}$/.test(idNumber)) {
                this.errors.idNumber = 'PAN must be in format: ABCDE1234F (5 letters + 4 digits + 1 letter)';
                return false;
            }
        }

        delete this.errors.idNumber;
        return true;
    }

    validateAddress(): boolean {
        const address = this.user.address.trim();
        
        if (!address) {
            this.errors.address = 'Address is required';
            return false;
        }
        
        if (address.length < 10) {
            this.errors.address = 'Address must be at least 10 characters';
            return false;
        }
        
        if (address.length > 200) {
            this.errors.address = 'Address cannot exceed 200 characters';
            return false;
        }

        delete this.errors.address;
        return true;
    }

    validatePassword(): boolean {
        const password = this.user.password;
        
        if (!password) {
            this.errors.password = 'Password is required';
            return false;
        }
        
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
        if (!passwordRegex.test(password)) {
            this.errors.password = 'Password must contain 8+ characters, uppercase, lowercase, number & special character';
            return false;
        }

        delete this.errors.password;
        return true;
    }

    validateConfirmPassword(): boolean {
        if (!this.user.confirmPassword) {
            this.errors.confirmPassword = 'Please confirm your password';
            return false;
        }
        
        if (this.user.password !== this.user.confirmPassword) {
            this.errors.confirmPassword = 'Passwords do not match';
            return false;
        }

        delete this.errors.confirmPassword;
        return true;
    }

    validateForm(): boolean {
        this.errors = {};
        let isValid = true;

        if (!this.validateFullName()) isValid = false;
        if (!this.validateMobile()) isValid = false;
        if (!this.validateEmail()) isValid = false;
        if (!this.validateIdNumber()) isValid = false;
        if (!this.validateAddress()) isValid = false;
        if (!this.validatePassword()) isValid = false;
        if (!this.validateConfirmPassword()) isValid = false;

        if (!this.user.accountType || (this.user.accountType !== 'SAVINGS' && this.user.accountType !== 'CURRENT')) {
            this.errors.accountType = 'Please select a valid account type';
            isValid = false;
        }

        if (!this.user.idType || (this.user.idType !== 'AADHAR' && this.user.idType !== 'PAN')) {
            this.errors.idType = 'Please select a valid ID type';
            isValid = false;
        }

        return isValid;
    }

    togglePasswordHints(): void {
        this.showPasswordHints = !this.showPasswordHints;
    }

    getPasswordStrength(): string {
        const password = this.user.password;
        if (!password) return '';
        
        let score = 0;
        if (this.hasMinLength()) score++;
        if (this.hasLowercase()) score++;
        if (this.hasUppercase()) score++;
        if (this.hasDigit()) score++;
        if (this.hasSpecialChar()) score++;

        if (score <= 2) return 'weak';
        if (score <= 3) return 'medium';
        if (score <= 4) return 'strong';
        return 'very-strong';
    }

    register(): void {
        if (this.isSubmitting) return;

        if (!this.validateForm()) {
            alert('❌ Please fix all validation errors before submitting');
            return;
        }

        this.isSubmitting = true;

        const userData = {
            ...this.user,
            fullName: this.user.fullName.trim(),
            mobile: this.user.mobile.trim(),
            email: this.user.email.trim().toLowerCase(),
            idNumber: this.user.idNumber.trim().toUpperCase(),
            address: this.user.address.trim()
        };

        this.auth.register(userData).subscribe({
            next: (res: any) => {
                alert(`🎉 Registration Successful!\n\n👤 Customer ID: ${res.customerId}\n🏦 Account Number: ${res.accountNumber}\n💰 Initial Balance: ₹0.00\n\n⏳ Please wait for admin approval to start banking.`);
                this.router.navigate(['/login']);
            },
            error: (err) => {
                this.isSubmitting = false;
                console.error('Registration error:', err);
                
                if (err?.error?.message) {
                    if (err.error.message.includes('email')) {
                        this.errors.email = 'Email already exists';
                    } else if (err.error.message.includes('mobile')) {
                        this.errors.mobile = 'Mobile number already exists';
                    } else {
                        alert(err.error.message);
                    }
                } else {
                    alert('❌ Registration failed. Please check all fields and try again.');
                }
            }
        });
    }

    goToLogin(): void {
        this.router.navigate(['/login']);
    }
}