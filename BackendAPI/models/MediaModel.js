let mediaFiles = [];

const MediaModel = {
    getAll: () => {
        return mediaFiles;
    },

    getById: (id) => {
        return mediaFiles.find(
            media => media.id === Number(id)
        );
    },

    create: (mediaData) => {
        const newMedia = {
            id: mediaFiles.length + 1,
            title: mediaData.title,
            type: mediaData.type,
            url: mediaData.url,
            description: mediaData.description || "",
            createdAt: new Date()
        };

        mediaFiles.push(newMedia);

        return newMedia;
    }
};

module.exports = MediaModel;
