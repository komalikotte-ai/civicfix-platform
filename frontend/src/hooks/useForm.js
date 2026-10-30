import { useState, useCallback } from 'react';

export const validators = {
  required: (message = 'This field is required') => (value) => {
    if (value === undefined || value === null || (typeof value === 'string' && value.trim() === '')) {
      return message;
    }
    return null;
  },

  email: (message = 'Please enter a valid email address') => (value) => {
    if (!value) return null;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(value) ? null : message;
  },

  minLength: (min, message) => (value) => {
    if (!value) return null;
    const msg = message || `Must be at least ${min} characters`;
    return String(value).length >= min ? null : msg;
  },

  maxLength: (max, message) => (value) => {
    if (!value) return null;
    const msg = message || `Must be at most ${max} characters`;
    return String(value).length <= max ? null : msg;
  },

  phone: (message = 'Please enter a valid 10-digit phone number') => (value) => {
    if (!value) return null;
    const phoneRegex = /^[0-9]{10}$/;
    const cleaned = String(value).replace(/[\s\-()]/g, '');
    return phoneRegex.test(cleaned) ? null : message;
  },

  pattern: (regex, message = 'Invalid format') => (value) => {
    if (!value) return null;
    return regex.test(value) ? null : message;
  },

  matches: (targetField, message) => (value, allValues) => {
    const msg = message || `Must match ${targetField}`;
    return value === allValues[targetField] ? null : msg;
  },
};

export const useForm = ({ initialValues = {}, validationRules = {}, onSubmit }) => {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validateField = useCallback((name, value, currentValues = values) => {
    const rules = validationRules[name];
    if (!rules) return null;

    const ruleArray = Array.isArray(rules) ? rules : [rules];
    for (const rule of ruleArray) {
      if (typeof rule === 'function') {
        const error = rule(value, currentValues);
        if (error) return error;
      }
    }
    return null;
  }, [validationRules, values]);

  const validateAll = useCallback((currentValues = values) => {
    const newErrors = {};
    Object.keys(validationRules).forEach((name) => {
      const error = validateField(name, currentValues[name], currentValues);
      if (error) {
        newErrors[name] = error;
      }
    });
    return newErrors;
  }, [validateField, validationRules, values]);

  const handleChange = useCallback((e) => {
    const { name, value, type, checked } = e.target;
    const val = type === 'checkbox' ? checked : value;

    setValues((prev) => {
      const updated = { ...prev, [name]: val };
      if (touched[name]) {
        const fieldError = validateField(name, val, updated);
        setErrors((errs) => ({
          ...errs,
          [name]: fieldError,
        }));
      }
      return updated;
    });
  }, [touched, validateField]);

  const handleBlur = useCallback((e) => {
    const { name, value } = e.target;
    setTouched((prev) => ({ ...prev, [name]: true }));
    const fieldError = validateField(name, value);
    setErrors((prev) => ({
      ...prev,
      [name]: fieldError,
    }));
  }, [validateField]);

  const setFieldValue = useCallback((name, value) => {
    setValues((prev) => {
      const updated = { ...prev, [name]: value };
      const fieldError = validateField(name, value, updated);
      setErrors((errs) => ({ ...errs, [name]: fieldError }));
      return updated;
    });
  }, [validateField]);

  const setFieldError = useCallback((name, errorMessage) => {
    setErrors((prev) => ({ ...prev, [name]: errorMessage }));
  }, []);

  const handleSubmit = useCallback(async (e) => {
    if (e && e.preventDefault) e.preventDefault();

    // Mark all as touched
    const allTouched = Object.keys(validationRules).reduce((acc, key) => {
      acc[key] = true;
      return acc;
    }, {});
    setTouched(allTouched);

    const validationErrors = validateAll(values);
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length > 0) {
      return false;
    }

    if (onSubmit) {
      setIsSubmitting(true);
      try {
        await onSubmit(values);
        return true;
      } catch (err) {
        throw err;
      } finally {
        setIsSubmitting(false);
      }
    }
    return true;
  }, [onSubmit, validateAll, validationRules, values]);

  const resetForm = useCallback(() => {
    setValues(initialValues);
    setErrors({});
    setTouched({});
    setIsSubmitting(false);
  }, [initialValues]);

  const isValid = Object.values(errors).every((err) => !err);

  return {
    values,
    errors,
    touched,
    isSubmitting,
    isValid,
    handleChange,
    handleBlur,
    handleSubmit,
    setFieldValue,
    setFieldError,
    resetForm,
    setValues,
  };
};

export default useForm;
