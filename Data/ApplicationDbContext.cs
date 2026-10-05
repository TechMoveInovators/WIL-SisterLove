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

        public DbSet<Role> Roles { get; set; }

        public DbSet<User> Users { get; set; }

        public DbSet<Donation> Donations { get; set; }

        public DbSet<Event> Events { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            modelBuilder.Entity<Role>()
                .HasIndex(r => r.RoleName)
                .IsUnique();

            modelBuilder.Entity<User>()
                .HasOne(u => u.Role)
                .WithMany(r => r.Users)
                .HasForeignKey(u => u.RoleId)
                .OnDelete(DeleteBehavior.Restrict);

            modelBuilder.Entity<Donation>()
                .HasIndex(d => d.TransactionId)
                .IsUnique();

            modelBuilder.Entity<Donation>()
                .HasIndex(d => d.Email);

            modelBuilder.Entity<Donation>()
                .HasIndex(d => d.CreatedAt);

            modelBuilder.Entity<Event>()
                .HasOne(e => e.CreatedByUser)
                .WithMany()
                .HasForeignKey(e => e.CreatedByUserId)
                .OnDelete(DeleteBehavior.SetNull);

            modelBuilder.Entity<Event>()
                .HasIndex(e => e.EventDate);

            modelBuilder.Entity<Event>()
                .HasIndex(e => e.EventName);
        }
    }
}