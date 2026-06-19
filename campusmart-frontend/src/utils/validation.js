export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validatePassword = (password) => {
  return password.length >= 6;
};

export const validatePhoneNumber = (phone) => {
  const phoneRegex = /^[0-9]{10}$/;
  return phoneRegex.test(phone.replace(/\D/g, ''));
};

export const validateFirstName = (name) => {
  return name.trim().length >= 2 && name.trim().length <= 50;
};

export const validateLastName = (name) => {
  return name.trim().length >= 2 && name.trim().length <= 50;
};

export const validateLoginForm = (email, password) => {
  const errors = {};

  if (!email) {
    errors.email = 'Email is required';
  } else if (!validateEmail(email)) {
    errors.email = 'Invalid email format';
  }

  if (!password) {
    errors.password = 'Password is required';
  } else if (!validatePassword(password)) {
    errors.password = 'Password must be at least 6 characters';
  }

  return errors;
};

export const validateRegisterForm = (formData) => {
  const { firstName, lastName, email, password, phoneNumber, roles } = formData;
  const errors = {};

  if (!firstName) {
    errors.firstName = 'First name is required';
  } else if (!validateFirstName(firstName)) {
    errors.firstName = 'First name must be 2-50 characters';
  }

  if (!lastName) {
    errors.lastName = 'Last name is required';
  } else if (!validateLastName(lastName)) {
    errors.lastName = 'Last name must be 2-50 characters';
  }

  if (!email) {
    errors.email = 'Email is required';
  } else if (!validateEmail(email)) {
    errors.email = 'Invalid email format';
  }

  if (!password) {
    errors.password = 'Password is required';
  } else if (!validatePassword(password)) {
    errors.password = 'Password must be at least 6 characters';
  }

  if (!phoneNumber) {
    errors.phoneNumber = 'Phone number is required';
  } else if (!validatePhoneNumber(phoneNumber)) {
    errors.phoneNumber = 'Phone number must be 10 digits';
  }

  if (!roles || roles.length === 0) {
    errors.roles = 'Please select at least one role';
  }

  return errors;
};
