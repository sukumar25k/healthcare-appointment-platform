import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test('user should register successfully', async ({ page }) => {
    await page.goto('/register');
    
    const uniqueEmail = `user-${Date.now()}@example.com`;
    await page.fill('input[type="text"]', 'Test User');
    await page.fill('input[type="email"]', uniqueEmail);
    await page.fill('input[type="password"]', 'securePass123');
    
    await page.click('button:has-text("Register")');
    
    await expect(page).toHaveURL('/dashboard', { timeout: 10000 });
    await expect(page.locator('text=Welcome, Test User')).toBeVisible();
  });

  test('user should login successfully', async ({ page }) => {
    // First register
    await page.goto('/register');
    const uniqueEmail = `login-${Date.now()}@example.com`;
    await page.fill('input[type="text"]', 'Login User');
    await page.fill('input[type="email"]', uniqueEmail);
    await page.fill('input[type="password"]', 'securePass123');
    await page.click('button:has-text("Register")');
    await expect(page).toHaveURL('/dashboard');
    
    // Logout
    await page.click('button:has-text("Logout")');
    await expect(page).toHaveURL('/login');
    
    // Login again
    await page.fill('input[type="email"]', uniqueEmail);
    await page.fill('input[type="password"]', 'securePass123');
    await page.click('button:has-text("Login")');
    
    await expect(page).toHaveURL('/dashboard');
    await expect(page.locator('text=Welcome, Login User')).toBeVisible();
  });

  test('login with wrong password should show error', async ({ page }) => {
    // First register a user
    await page.goto('/register');
    const uniqueEmail = `error-${Date.now()}@example.com`;
    await page.fill('input[type="text"]', 'Error Test');
    await page.fill('input[type="email"]', uniqueEmail);
    await page.fill('input[type="password"]', 'securePass123');
    await page.click('button:has-text("Register")');
    await expect(page).toHaveURL('/dashboard');
    await page.click('button:has-text("Logout")');
    
    // Try to login with wrong password
    await page.fill('input[type="email"]', uniqueEmail);
    await page.fill('input[type="password"]', 'wrongPassword');
    await page.click('button:has-text("Login")');
    
    await expect(page.locator('text=Invalid email or password')).toBeVisible();
  });

  test('should show validation error for short password', async ({ page }) => {
    await page.goto('/register');
    
    await page.fill('input[type="text"]', 'Test');
    await page.fill('input[type="email"]', `short-${Date.now()}@example.com`);
    await page.fill('input[type="password"]', '123');
    
    await page.click('button:has-text("Register")');
    
    await expect(page.locator('text=must be at least 6 characters')).toBeVisible();
  });
});
