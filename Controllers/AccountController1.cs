using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SisterLoveSA.Models;
using System.Security.Claims;

namespace SisterLoveSA.Controllers
{
    [Authorize]
    public class AccountController : Controller
    {
        // ============================================================
        // PROFILE - GET
        // ============================================================

        [HttpGet]
        public IActionResult Profile()
        {
            var model = new UserProfileViewModel
            {
                FullName = User.FindFirst(ClaimTypes.Name)?.Value ?? "User",

                Email = User.FindFirst(ClaimTypes.Email)?.Value
                        ?? "user@example.com",

                PhoneNumber = User.FindFirst(ClaimTypes.MobilePhone)?.Value
                        ?? "",

                MemberSince = DateTime.Now.AddMonths(-6)
            };

            return View(model);
        }

        // ============================================================
        // PROFILE - POST
        // ============================================================

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Profile(
            UserProfileViewModel model)
        {
            // Check that the submitted profile data is valid
            if (!ModelState.IsValid)
            {
                return View(model);
            }

            // Make sure the current user is authenticated
            var identity = User.Identity as ClaimsIdentity;

            if (identity == null || !identity.IsAuthenticated)
            {
                return Challenge();
            }

            // --------------------------------------------------------
            // UPDATE FULL NAME
            // --------------------------------------------------------

            var nameClaim = identity.FindFirst(ClaimTypes.Name);

            if (nameClaim != null)
            {
                identity.RemoveClaim(nameClaim);
            }

            identity.AddClaim(
                new Claim(
                    ClaimTypes.Name,
                    model.FullName
                )
            );

            // --------------------------------------------------------
            // UPDATE PHONE NUMBER
            // --------------------------------------------------------

            var phoneClaim =
                identity.FindFirst(ClaimTypes.MobilePhone);

            if (phoneClaim != null)
            {
                identity.RemoveClaim(phoneClaim);
            }

            if (!string.IsNullOrWhiteSpace(model.PhoneNumber))
            {
                identity.AddClaim(
                    new Claim(
                        ClaimTypes.MobilePhone,
                        model.PhoneNumber
                    )
                );
            }

            // --------------------------------------------------------
            // REFRESH AUTHENTICATION COOKIE
            // --------------------------------------------------------

            var principal = new ClaimsPrincipal(identity);

            await HttpContext.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                principal
            );

            TempData["SuccessMessage"] =
                "Your profile has been updated successfully.";

            return RedirectToAction(nameof(Profile));
        }

        // ============================================================
        // LOGIN
        // ============================================================

        [AllowAnonymous]
        [HttpGet]
        public async Task<IActionResult> Login()
        {
            /*
             * TEMPORARY DEMONSTRATION LOGIN
             *
             * This currently creates a test user.
             * Database-backed authentication will be implemented
             * separately.
             */

            var claims = new List<Claim>
            {
                new Claim(
                    ClaimTypes.Name,
                    "Asandak"
                ),

                new Claim(
                    ClaimTypes.Email,
                    "asandak@sisterlove.org.za"
                ),

                new Claim(
                    ClaimTypes.Role,
                    "User"
                )
            };

            var identity = new ClaimsIdentity(
                claims,
                CookieAuthenticationDefaults.AuthenticationScheme
            );

            var principal = new ClaimsPrincipal(identity);

            await HttpContext.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                principal
            );

            return RedirectToAction(
                "Index",
                "Home"
            );
        }

        // ============================================================
        // ACCESS DENIED
        // ============================================================

        [AllowAnonymous]
        [HttpGet]
        public IActionResult AccessDenied()
        {
            return View();
        }

        // ============================================================
        // LOGOUT
        // ============================================================

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Logout()
        {
            await HttpContext.SignOutAsync(
                CookieAuthenticationDefaults.AuthenticationScheme
            );

            return RedirectToAction(
                "Index",
                "Home"
            );
        }
    }
}