
const requireAdmin = (req, res, next) => {
    if (!req.user) {
        return res.status(401).json({
            message: "Authentication is required."
        });
    }

    if (Number(req.user.roleId) !== 2) {
        return res.status(403).json({
            message: "Administrator access is required."
        });
    }

    return next();
};

module.exports = requireAdmin;
