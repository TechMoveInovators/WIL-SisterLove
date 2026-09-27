using Microsoft.AspNetCore.Mvc;
using Sisterlove.Models;
using SisterLoveSA.Models;
using System.Diagnostics;

namespace SisterLoveSA.Controllers
{
    public class HomeController : Controller
    {
        private readonly ILogger<HomeController> _logger;

        public HomeController(ILogger<HomeController> logger)
        {
            _logger = logger;
        }

        public IActionResult Index() => View();

        public IActionResult About()
        {
            return View("About/Index");
        }

        public IActionResult OurWork()
        {
            return View("OurWork/Index");
        }

        public IActionResult Partnerships()
        {
            return View("Partnerships/Index");
        }

        public IActionResult TrackRecord()
        {
            return View("TrackRecord/Index");
        }

        public IActionResult GetInvolved()
        {
            return View("GetInvolved/Index");
        }

        public IActionResult Error()
        {
            return View(
                new ErrorViewModel
                {
                    RequestId = Activity.Current?.Id
                        ?? HttpContext.TraceIdentifier
                });
        }
    }
}