using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Sisterlove.Migrations
{
    /// <inheritdoc />
    public partial class AddDonationDetails : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "DonationType",
                table: "Donations",
                type: "nvarchar(20)",
                maxLength: 20,
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<string>(
                name: "Message",
                table: "Donations",
                type: "nvarchar(500)",
                maxLength: 500,
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "DonationType",
                table: "Donations");

            migrationBuilder.DropColumn(
                name: "Message",
                table: "Donations");
        }
    }
}
