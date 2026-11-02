
const { getPool, sql } = require('../config/db');

const DonationModel = {
    async getAll() {
        const pool = await getPool();

        const result = await pool.request().query(`
            SELECT
                Id AS id,
                DonorName AS name,
                Email AS email,
                Amount AS amount,
                Message AS message,
                DonationType AS donationType,
                PaymentMethod AS paymentMethod,
                TransactionId AS transactionId,
                CreatedAt AS createdAt,
                UserId AS userId
            FROM dbo.Donations
            ORDER BY CreatedAt DESC;
        `);

        return result.recordset;
    },

    async getById(id) {
        const pool = await getPool();

        const result = await pool.request()
            .input('id', sql.Int, Number(id))
            .query(`
                SELECT
                    Id AS id,
                    DonorName AS name,
                    Email AS email,
                    Amount AS amount,
                    Message AS message,
                    DonationType AS donationType,
                    PaymentMethod AS paymentMethod,
                    TransactionId AS transactionId,
                    CreatedAt AS createdAt,
                    UserId AS userId
                FROM dbo.Donations
                WHERE Id = @id;
            `);

        return result.recordset[0] || null;
    },

    async create(data) {
        const pool = await getPool();

        const result = await pool.request()
            .input('name', sql.NVarChar(100), data.name)
            .input('email', sql.NVarChar(150), data.email)
            .input('amount', sql.Decimal(18, 2), Number(data.amount))
            .input('message', sql.NVarChar(500), data.message || null)
            .input('donationType', sql.NVarChar(20), data.donationType || 'OneTime')
            .input('paymentMethod', sql.NVarChar(50), data.paymentMethod || null)
            .input('transactionId', sql.NVarChar(100), data.transactionId || null)
            .input('userId', sql.Int, data.userId ?? null)
            .query(`
                INSERT INTO dbo.Donations
                    (DonorName, Email, Amount, Message, DonationType,
                     PaymentMethod, TransactionId, CreatedAt, UserId)
                OUTPUT
                    INSERTED.Id AS id,
                    INSERTED.DonorName AS name,
                    INSERTED.Email AS email,
                    INSERTED.Amount AS amount,
                    INSERTED.Message AS message,
                    INSERTED.DonationType AS donationType,
                    INSERTED.PaymentMethod AS paymentMethod,
                    INSERTED.TransactionId AS transactionId,
                    INSERTED.CreatedAt AS createdAt,
                    INSERTED.UserId AS userId
                VALUES
                    (@name, @email, @amount, @message, @donationType,
                     @paymentMethod, @transactionId, SYSUTCDATETIME(), @userId);
            `);

        return result.recordset[0];
    }
};

module.exports = DonationModel;
