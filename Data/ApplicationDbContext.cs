using Microsoft.EntityFrameworkCore;
using Sisterlove.Models.Entities;

namespace Sisterlove.Data
{
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options)
            : base(options)
        {
        }

        public DbSet<Donation> Donations { get; set; }
    }
}