using Microsoft.AspNetCore.Mvc;

namespace SisterLoveSA.Controllers
{
    public class AboutController : Controller
    {
        public IActionResult Index() => View();
    }
}