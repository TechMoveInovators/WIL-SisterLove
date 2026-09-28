const authorizeAdmin = (req, res, next) => {

    if (!req.user) {
        return res.status(401).json({
            message: "Access denied. Please log in first."
        });
    }

    if (req.user.roleId !== 2) {
        return res.status(403).json({
            message: "Access denied. Administrator permissions required."
        });
    }

    next();
};

module.exports = authorizeAdmin;
