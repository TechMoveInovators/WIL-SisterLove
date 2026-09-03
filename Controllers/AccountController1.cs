using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Sisterlove.Models;
using SisterLoveSA.Models;
using System.Security.Claims;

namespace SisterLoveSA.Controllers
{
    [Authorize]
    public class AccountController : Controller
    {
        private readonly ILogger<AccountController> _logger;

        public AccountController(ILogger<AccountController> logger)
        {
            _logger = logger;
        }

        [HttpGet]
        public IActionResult Profile()
        {
            var user = GetCurrentUser();
            var model = new UserProfileViewModel
            {
                FullName = user.FindFirst(ClaimTypes.Name)?.Value ?? "User",
                Email = user.FindFirst(ClaimTypes.Email)?.Value ?? "user@example.com",
                PhoneNumber = user.FindFirst(ClaimTypes.MobilePhone)?.Value ?? "",
                MemberSince = DateTime.Now.AddMonths(-6) // Simulated
            };

            return View(model);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Profile(UserProfileViewModel model)
        {
            if (!ModelState.IsValid)
            {
                return View(model);
            }

            try
            {
                // In production, you would update the user in the database
                var user = GetCurrentUser();
                var identity = (ClaimsIdentity)user.Identity;

                // Update claims
                var nameClaim = identity.FindFirst(ClaimTypes.Name);
                if (nameClaim != null)
                    identity.RemoveClaim(nameClaim);
                identity.AddClaim(new Claim(ClaimTypes.Name, model.FullName));

                var phoneClaim = identity.FindFirst(ClaimTypes.MobilePhone);
                if (phoneClaim != null)
                    identity.RemoveClaim(phoneClaim);
                if (!string.IsNullOrEmpty(model.PhoneNumber))
                    identity.AddClaim(new Claim(ClaimTypes.MobilePhone, model.PhoneNumber));

                await HttpContext.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, new ClaimsPrincipal(identity));

                TempData["SuccessMessage"] = "Your profile has been updated successfully.";
                return RedirectToAction("Profile");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error updating profile");
                ModelState.AddModelError("", "An error occurred while updating your profile.");
                return View(model);
            }
        }

        [AllowAnonymous]
        public IActionResult Login()
        {
            // Simplified for prototype
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.Name, "Ben Brown"),
                new Claim(ClaimTypes.Email, "Bbrown@sisterlove.org.za"),
                new Claim(ClaimTypes.Role, "User"),
                new Claim("ProfileImage", "/images/default-avatar.png")
            };

            var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
            var principal = new ClaimsPrincipal(identity);

            HttpContext.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, principal);

            return RedirectToAction("Index", "Home");
        }

        public async Task<IActionResult> Logout()
        {
            await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
            return RedirectToAction("Index", "Home");
        }

        private ClaimsPrincipal GetCurrentUser()
        {
            return User;
        }
    }
}