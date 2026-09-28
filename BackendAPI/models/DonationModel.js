let donations = [];

const DonationModel = {
    getAll: () => {
        return donations;
    },

    getById: (id) => {
        return donations.find(
            donation => donation.id === Number(id)
        );
    },

    create: (donationData) => {
        const newDonation = {
            id: donations.length + 1,
            name: donationData.name,
            email: donationData.email,
            amount: donationData.amount,
            message: donationData.message || "",
            status: "Pending",
            createdAt: new Date()
        };

        donations.push(newDonation);

        return newDonation;
    }
};

module.exports = DonationModel;
