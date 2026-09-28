let volunteerApplications = [];

const VolunteerModel = {

    // Get all volunteers
    getAll: () => {
        return volunteerApplications;
    },

    // Get volunteer by ID
    getById: (id) => {
        return volunteerApplications.find(
            volunteer => volunteer.id === Number(id)
        );
    },

    // Create volunteer
    create: (volunteerData) => {

        const newVolunteer = {
            id: volunteerApplications.length + 1,
            name: volunteerData.name,
            email: volunteerData.email,
            phone: volunteerData.phone,
            areaOfInterest: volunteerData.areaOfInterest,
            motivation: volunteerData.motivation,
            status: "New",
            createdAt: new Date()
        };

        volunteerApplications.push(newVolunteer);

        return newVolunteer;
    }
};

module.exports = VolunteerModel;