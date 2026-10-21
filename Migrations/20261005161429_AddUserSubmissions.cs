using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Sisterlove.Migrations
{
    /// <inheritdoc />
    public partial class AddUserSubmissions : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "ContactEnquiries",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    FullName = table.Column<string>(type: "nvarchar(150)", maxLength: 150, nullable: false),
                    Email = table.Column<string>(type: "nvarchar(150)", maxLength: 150, nullable: false),
                    PhoneNumber = table.Column<string>(type: "nvarchar(30)", maxLength: 30, nullable: true),
                    Subject = table.Column<string>(type: "nvarchar(200)", maxLength: 200, nullable: false),
                    Message = table.Column<string>(type: "nvarchar(3000)", maxLength: 3000, nullable: false),
                    SubmittedAt = table.Column<DateTime>(type: "datetime2", nullable: false),
                    UserId = table.Column<int>(type: "int", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_ContactEnquiries", x => x.Id);
                    table.ForeignKey(
                        name: "FK_ContactEnquiries_Users_UserId",
                        column: x => x.UserId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.SetNull);
                });

            migrationBuilder.CreateTable(
                name: "FundingEnquiries",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    FullName = table.Column<string>(type: "nvarchar(150)", maxLength: 150, nullable: false),
                    Email = table.Column<string>(type: "nvarchar(150)", maxLength: 150, nullable: false),
                    PhoneNumber = table.Column<string>(type: "nvarchar(30)", maxLength: 30, nullable: true),
                    Enquiry = table.Column<string>(type: "nvarchar(2000)", maxLength: 2000, nullable: false),
                    SubmittedAt = table.Column<DateTime>(type: "datetime2", nullable: false),
                    UserId = table.Column<int>(type: "int", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_FundingEnquiries", x => x.Id);
                    table.ForeignKey(
                        name: "FK_FundingEnquiries_Users_UserId",
                        column: x => x.UserId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.SetNull);
                });

            migrationBuilder.CreateTable(
                name: "VolunteerApplications",
                columns: table => new
                {
                    Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("SqlServer:Identity", "1, 1"),
                    FullName = table.Column<string>(type: "nvarchar(150)", maxLength: 150, nullable: false),
                    Email = table.Column<string>(type: "nvarchar(150)", maxLength: 150, nullable: false),
                    PhoneNumber = table.Column<string>(type: "nvarchar(30)", maxLength: 30, nullable: true),
                    Motivation = table.Column<string>(type: "nvarchar(2000)", maxLength: 2000, nullable: false),
                    SubmittedAt = table.Column<DateTime>(type: "datetime2", nullable: false),
                    UserId = table.Column<int>(type: "int", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_VolunteerApplications", x => x.Id);
                    table.ForeignKey(
                        name: "FK_VolunteerApplications_Users_UserId",
                        column: x => x.UserId,
                        principalTable: "Users",
                        principalColumn: "Id",
                        onDelete: ReferentialAction.SetNull);
                });

            migrationBuilder.CreateIndex(
                name: "IX_ContactEnquiries_Email",
                table: "ContactEnquiries",
                column: "Email");

            migrationBuilder.CreateIndex(
                name: "IX_ContactEnquiries_SubmittedAt",
                table: "ContactEnquiries",
                column: "SubmittedAt");

            migrationBuilder.CreateIndex(
                name: "IX_ContactEnquiries_UserId",
                table: "ContactEnquiries",
                column: "UserId");

            migrationBuilder.CreateIndex(
                name: "IX_FundingEnquiries_Email",
                table: "FundingEnquiries",
                column: "Email");

            migrationBuilder.CreateIndex(
                name: "IX_FundingEnquiries_SubmittedAt",
                table: "FundingEnquiries",
                column: "SubmittedAt");

            migrationBuilder.CreateIndex(
                name: "IX_FundingEnquiries_UserId",
                table: "FundingEnquiries",
                column: "UserId");

            migrationBuilder.CreateIndex(
                name: "IX_VolunteerApplications_Email",
                table: "VolunteerApplications",
                column: "Email");

            migrationBuilder.CreateIndex(
                name: "IX_VolunteerApplications_SubmittedAt",
                table: "VolunteerApplications",
                column: "SubmittedAt");

            migrationBuilder.CreateIndex(
                name: "IX_VolunteerApplications_UserId",
                table: "VolunteerApplications",
                column: "UserId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "ContactEnquiries");

            migrationBuilder.DropTable(
                name: "FundingEnquiries");

            migrationBuilder.DropTable(
                name: "VolunteerApplications");
        }
    }
}
