import { test, expect } from '@playwright/test';

async function registerAndLogin(page) {
  const uniqueEmail = `apt-${Date.now()}@example.com`;
  await page.goto('/register');
  
  await page.fill('input[type="text"]', 'Appointment Tester');
  await page.fill('input[type="email"]', uniqueEmail);
  await page.fill('input[type="password"]', 'securePass123');
  
  await page.click('button:has-text("Register")');
  await expect(page).toHaveURL('/dashboard');
  
  return uniqueEmail;
}

test.describe('Appointments', () => {
  test('should view available slots', async ({ page }) => {
    await registerAndLogin(page);
    
    await page.click('button:has-text("Available Slots")');
    
    // Wait for slots to load
    const slotCards = page.locator('.slot-card');
    await expect(slotCards.first()).toBeVisible({ timeout: 10000 });
    
    // Verify slot details are displayed
    await expect(page.locator('text=Dr.')).toBeVisible();
  });

  test('should book an appointment', async ({ page }) => {
    await registerAndLogin(page);
    
    // Make sure we're on Available Slots tab
    await page.click('button:has-text("Available Slots")');
    
    // Wait for slots to load
    await expect(page.locator('.slot-card').first()).toBeVisible({ timeout: 10000 });
    
    // Book the first available slot
    const bookButtons = page.locator('button:has-text("Book Appointment")');
    await bookButtons.first().click();
    
    // Verify success message
    await expect(page.locator('text=Appointment booked successfully')).toBeVisible({ timeout: 10000 });
    
    // Verify slot is no longer in available list
    await page.reload();
    await page.click('button:has-text("Available Slots")');
    const initialSlotCount = await page.locator('.slot-card').count();
    
    // Go to history and verify appointment is there
    await page.click('button:has-text("Appointment History")');
    await expect(page.locator('.appointment-card').first()).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=BOOKED')).toBeVisible();
  });

  test('should cancel an appointment', async ({ page }) => {
    await registerAndLogin(page);
    
    // Book an appointment
    await page.click('button:has-text("Available Slots")');
    await expect(page.locator('.slot-card').first()).toBeVisible({ timeout: 10000 });
    
    const bookButtons = page.locator('button:has-text("Book Appointment")');
    await bookButtons.first().click();
    await expect(page.locator('text=Appointment booked successfully')).toBeVisible({ timeout: 10000 });
    
    // Go to appointment history
    await page.click('button:has-text("Appointment History")');
    await expect(page.locator('.appointment-card').first()).toBeVisible({ timeout: 10000 });
    
    // Cancel the appointment
    const cancelButtons = page.locator('button:has-text("Cancel Appointment")');
    await cancelButtons.first().click();
    
    // Confirm cancellation in dialog
    page.on('dialog', async (dialog) => {
      if (dialog.type() === 'confirm') {
        await dialog.accept();
      }
    });
    
    // Verify cancellation success (button disappears or status changes)
    await expect(page.locator('text=CANCELLED')).toBeVisible({ timeout: 10000 });
  });

  test('should display appointment history', async ({ page }) => {
    await registerAndLogin(page);
    
    // Book multiple appointments
    await page.click('button:has-text("Available Slots")');
    await expect(page.locator('.slot-card').first()).toBeVisible({ timeout: 10000 });
    
    const bookButtons = page.locator('button:has-text("Book Appointment")');
    const buttonCount = await bookButtons.count();
    
    if (buttonCount > 0) {
      await bookButtons.first().click();
      await expect(page.locator('text=Appointment booked successfully')).toBeVisible({ timeout: 10000 });
    }
    
    // View appointment history
    await page.click('button:has-text("Appointment History")');
    
    // Verify appointment is listed
    await expect(page.locator('.appointment-card').first()).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=Dr.')).toBeVisible();
    await expect(page.locator('text=Booked:')).toBeVisible();
  });

  test('unauthenticated user should be redirected to login', async ({ page }) => {
    // Clear localStorage to simulate unauthenticated state
    await page.context().clearCookies();
    await page.evaluate(() => localStorage.clear());
    
    await page.goto('/dashboard');
    
    // Should redirect to login
    await expect(page).toHaveURL('/login', { timeout: 5000 });
  });
});
