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
        [HttpGet]
        public IActionResult Profile()
        {
            var model = new UserProfileViewModel
            {
                FullName = User.FindFirst(ClaimTypes.Name)?.Value ?? "User",
                Email = User.FindFirst(ClaimTypes.Email)?.Value ?? "user@example.com",
                PhoneNumber = User.FindFirst(ClaimTypes.MobilePhone)?.Value ?? "",
                MemberSince = DateTime.Now.AddMonths(-6)
            };
            return View(model);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Profile(UserProfileViewModel model)
        {
            if (!ModelState.IsValid)
                return View(model);

            var identity = (ClaimsIdentity)User.Identity!;

            var nameClaim = identity.FindFirst(ClaimTypes.Name);
            if (nameClaim != null) identity.RemoveClaim(nameClaim);
            identity.AddClaim(new Claim(ClaimTypes.Name, model.FullName));

            var phoneClaim = identity.FindFirst(ClaimTypes.MobilePhone);
            if (phoneClaim != null) identity.RemoveClaim(phoneClaim);
            if (!string.IsNullOrEmpty(model.PhoneNumber))
                identity.AddClaim(new Claim(ClaimTypes.MobilePhone, model.PhoneNumber));

            await HttpContext.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, new ClaimsPrincipal(identity));

            TempData["SuccessMessage"] = "Your profile has been updated successfully.";
            return RedirectToAction("Profile");
        }

        [AllowAnonymous]
        public async Task<IActionResult> Login()
        {
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.Name, "Asandak"),
                new Claim(ClaimTypes.Email, "asandak@sisterlove.org.za"),
                new Claim(ClaimTypes.Role, "User")
            };

            var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
            await HttpContext.SignInAsync(CookieAuthenticationDefaults.AuthenticationScheme, new ClaimsPrincipal(identity));

            return RedirectToAction("Index", "Home");
        }

        public async Task<IActionResult> Logout()
        {
            await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
            return RedirectToAction("Index", "Home");
        }
    }
}