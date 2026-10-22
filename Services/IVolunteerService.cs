using SisterLoveSA.Models;
namespace Sisterlove.Services
{
    public interface IVolunteerService
    {
        Task<bool> SubmitVolunteerApplicationAsync(VolunteerViewModel model);
    }
}
