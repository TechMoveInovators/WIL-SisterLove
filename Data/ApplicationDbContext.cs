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

        public DbSet<Programme> Programmes { get; set; }

        public DbSet<Partnership> Partnerships { get; set; }

        public DbSet<LegacyProject> LegacyProjects { get; set; }

        public DbSet<NewsArticle> NewsArticles { get; set; }

        public DbSet<Media> Media { get; set; }

        public DbSet<VolunteerApplication> VolunteerApplications { get; set; }

        public DbSet<FundingEnquiry> FundingEnquiries { get; set; }

        public DbSet<ContactEnquiry> ContactEnquiries { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Role → User
            modelBuilder.Entity<Role>()
                .HasIndex(r => r.RoleName)
                .IsUnique();

            modelBuilder.Entity<User>()
                .HasOne(u => u.Role)
                .WithMany(r => r.Users)
                .HasForeignKey(u => u.RoleId)
                .OnDelete(DeleteBehavior.Restrict);

            // Donation indexes
            modelBuilder.Entity<Donation>()
                .HasIndex(d => d.TransactionId)
                .IsUnique();

            modelBuilder.Entity<Donation>()
                .HasIndex(d => d.Email);

            modelBuilder.Entity<Donation>()
                .HasIndex(d => d.CreatedAt);

            // Event → User
            modelBuilder.Entity<Event>()
                .HasOne(e => e.CreatedByUser)
                .WithMany()
                .HasForeignKey(e => e.CreatedByUserId)
                .OnDelete(DeleteBehavior.SetNull);

            modelBuilder.Entity<Event>()
                .HasIndex(e => e.EventDate);

            modelBuilder.Entity<Event>()
                .HasIndex(e => e.EventName);

            // User → Programme
            modelBuilder.Entity<Programme>()
                .HasOne(p => p.CreatedByUser)
                .WithMany()
                .HasForeignKey(p => p.CreatedByUserId)
                .OnDelete(DeleteBehavior.SetNull);

            modelBuilder.Entity<Programme>()
                .HasIndex(p => p.ProgrammeName);

            // User → Partnership
            modelBuilder.Entity<Partnership>()
                .HasOne(p => p.CreatedByUser)
                .WithMany()
                .HasForeignKey(p => p.CreatedByUserId)
                .OnDelete(DeleteBehavior.SetNull);

            modelBuilder.Entity<Partnership>()
                .HasIndex(p => p.OrganisationName);

            // User → LegacyProject
            modelBuilder.Entity<LegacyProject>()
                .HasOne(l => l.CreatedByUser)
                .WithMany()
                .HasForeignKey(l => l.CreatedByUserId)
                .OnDelete(DeleteBehavior.SetNull);

            modelBuilder.Entity<LegacyProject>()
                .HasIndex(l => l.ProjectName);

            // User → NewsArticle
            modelBuilder.Entity<NewsArticle>()
                .HasOne(n => n.CreatedByUser)
                .WithMany()
                .HasForeignKey(n => n.CreatedByUserId)
                .OnDelete(DeleteBehavior.SetNull);

            modelBuilder.Entity<NewsArticle>()
                .HasIndex(n => n.PublishedAt);

            // Programme → Media
            modelBuilder.Entity<Media>()
                .HasOne(m => m.Programme)
                .WithMany(p => p.Media)
                .HasForeignKey(m => m.ProgrammeId)
                .OnDelete(DeleteBehavior.Cascade);

            // NewsArticle → Media
            modelBuilder.Entity<Media>()
                .HasOne(m => m.NewsArticle)
                .WithMany(n => n.Media)
                .HasForeignKey(m => m.NewsArticleId)
                .OnDelete(DeleteBehavior.Cascade);

            // LegacyProject → Media
            modelBuilder.Entity<Media>()
                .HasOne(m => m.LegacyProject)
                .WithMany(l => l.Media)
                .HasForeignKey(m => m.LegacyProjectId)
                .OnDelete(DeleteBehavior.Cascade);

            modelBuilder.Entity<Media>()
                .HasIndex(m => m.FileName);

            modelBuilder.Entity<Media>()
                .HasIndex(m => m.MediaType);

            // User → VolunteerApplication
            modelBuilder.Entity<VolunteerApplication>()
                .HasOne(v => v.User)
                .WithMany()
                .HasForeignKey(v => v.UserId)
                .OnDelete(DeleteBehavior.SetNull);

            modelBuilder.Entity<VolunteerApplication>()
                .HasIndex(v => v.Email);

            modelBuilder.Entity<VolunteerApplication>()
                .HasIndex(v => v.SubmittedAt);

            // User → FundingEnquiry
            modelBuilder.Entity<FundingEnquiry>()
                .HasOne(f => f.User)
                .WithMany()
                .HasForeignKey(f => f.UserId)
                .OnDelete(DeleteBehavior.SetNull);

            modelBuilder.Entity<FundingEnquiry>()
                .HasIndex(f => f.Email);

            modelBuilder.Entity<FundingEnquiry>()
                .HasIndex(f => f.SubmittedAt);

            // User → ContactEnquiry
            modelBuilder.Entity<ContactEnquiry>()
                .HasOne(c => c.User)
                .WithMany()
                .HasForeignKey(c => c.UserId)
                .OnDelete(DeleteBehavior.SetNull);

            modelBuilder.Entity<ContactEnquiry>()
                .HasIndex(c => c.Email);

            modelBuilder.Entity<ContactEnquiry>()
                .HasIndex(c => c.SubmittedAt);

            // Seed system roles
            modelBuilder.Entity<Role>().HasData(
                new Role
                {
                    Id = 1,
                    RoleName = "User",
                    Description = "Standard website user"
                },
                new Role
                {
                    Id = 2,
                    RoleName = "Administrator",
                    Description = "Administrator with access to manage content and view submissions"
                }
            );
        }


    }
}