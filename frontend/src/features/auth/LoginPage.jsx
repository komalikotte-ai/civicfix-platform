import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth.js';
import Card from '../../components/common/Card.jsx';
import Input from '../../components/common/Input.jsx';
import Button from '../../components/common/Button.jsx';
import Alert from '../../components/common/Alert.jsx';

export const LoginPage = () => {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [sessionExpiredNotice, setSessionExpiredNotice] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('session_expired') === 'true') {
      setSessionExpiredNotice(true);
    }
  }, [location.search]);

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
          <div className="inline-flex h-12 w-12 rounded-xl bg-blue-600 text-white font-black items-center justify-center text-xl mb-2 shadow-xs">
            CF
          </div>
          <h2 className="text-2xl font-black text-slate-900 tracking-tight">Sign in to CivicFix</h2>
          <p className="text-xs text-slate-500 mt-1">
            Municipal Complaint & City Operations Management Platform
          </p>
        </div>

        <Card>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            {sessionExpiredNotice && (
              <Alert
                type="warning"
                message="Your session expired due to inactivity. Please sign in again to continue."
                onClose={() => setSessionExpiredNotice(false)}
              />
            )}

            {errorMessage && <Alert type="error" message={errorMessage} onClose={() => setErrorMessage('')} />}

            <Input
              label="Username or Email"
              id="usernameOrEmail"
              name="usernameOrEmail"
              value={usernameOrEmail}
              onChange={(e) => setUsernameOrEmail(e.target.value)}
              placeholder="e.g. citizen1 or admin@city.gov"
              required
              autoComplete="username"
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
              autoComplete="current-password"
            />

            <Button type="submit" variant="primary" loading={loading} className="w-full mt-2 font-bold shadow-xs">
              Sign In
            </Button>
          </form>

          <div className="mt-4 pt-4 border-t border-slate-100 text-center text-xs text-slate-600 flex flex-col gap-2">
            <div>
              Don't have an account?{' '}
              <Link to="/register" className="font-bold text-blue-600 hover:text-blue-800">
                Register as Citizen
              </Link>
            </div>
            <div>
              <Link to="/transparency" className="text-slate-500 hover:underline">
                View Open Governance & Transparency Portal →
              </Link>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default LoginPage;
