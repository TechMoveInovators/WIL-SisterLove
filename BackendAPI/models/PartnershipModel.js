let partnerships = [];

const PartnershipModel = {

    // Get all partnerships
    getAll: () => {
        return partnerships;
    },

    // Get partnership by ID
    getById: (id) => {
        return partnerships.find(
            partnership => partnership.id === Number(id)
        );
    },

    // Create partnership
    create: (partnershipData) => {

        const newPartnership = {
            id: partnerships.length + 1,
            name: partnershipData.name,
            description: partnershipData.description,
            logo: partnershipData.logo || "",
            website: partnershipData.website || "",
            status: "Active",
            createdAt: new Date()
        };

        partnerships.push(newPartnership);

        return newPartnership;
    }

};

module.exports = PartnershipModel;