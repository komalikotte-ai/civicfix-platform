import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import Card from '../../components/common/Card';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';
import Alert from '../../components/common/Alert';

export const LoginPage = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!usernameOrEmail || !password) {
      setErrorMessage('Please fill in both username/email and password');
      return;
    }

    setLoading(true);
    setErrorMessage('');
    try {
      await login(usernameOrEmail, password);
      navigate(from, { replace: true });
    } catch (err) {
      setErrorMessage(err.message || 'Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4">
      <div className="max-w-md w-full">
        <div className="text-center mb-6">
          <div className="inline-flex h-12 w-12 rounded-lg bg-blue-600 text-white font-bold items-center justify-center text-xl mb-2">
            CF
          </div>
          <h2 className="text-2xl font-bold text-gray-900">Sign in to CivicFix</h2>
          <p className="text-xs text-gray-500 mt-1">
            Municipal Complaint & City Operations Management Platform
          </p>
        </div>

        <Card>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            {errorMessage && <Alert type="error" message={errorMessage} />}

            <Input
              label="Username or Email"
              id="usernameOrEmail"
              name="usernameOrEmail"
              value={usernameOrEmail}
              onChange={(e) => setUsernameOrEmail(e.target.value)}
              placeholder="e.g. citizen1 or admin@city.gov"
              required
            />

            <Input
              label="Password"
              id="password"
              name="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
            />

            <Button type="submit" loading={loading} className="w-full mt-2">
              Sign In
            </Button>
          </form>

          <div className="mt-4 pt-4 border-t border-gray-100 text-center text-xs text-gray-600">
            Don't have an account?{' '}
            <Link to="/register" className="font-semibold text-blue-600 hover:text-blue-800">
              Register as Citizen
            </Link>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default LoginPage;
