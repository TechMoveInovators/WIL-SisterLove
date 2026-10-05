using Microsoft.AspNetCore.Mvc;
using Sisterlove.Models;
using SisterLoveSA.Models;
using System.Collections.Generic;
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

        public IActionResult Index()
        {
            // Pass the slideshow image names to the view
            var slideshowImages = new List<string>
            {
                "slide1.jpg",
                "slide2.jpg",
                "slide3.jpg",
                "slide4.jpg",
                "slide5.jpg"
            };

            return View(slideshowImages);
        }

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