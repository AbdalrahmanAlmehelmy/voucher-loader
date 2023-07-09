import java.sql.Timestamp;

public class Voucher {

    public long voucher_serial;
    public int voucher_pin, voucher_package;
    public Timestamp created_at;
    public String created_by;
    public Voucher() {
        voucher_serial = 0;
        voucher_pin = voucher_package = 0;
        created_at = new Timestamp(0);
        created_by = "null";
    };
    public Voucher(long voucher_serial, int voucher_pin, int voucher_package, Timestamp created_at, String created_by) {
        this.voucher_serial = voucher_serial;
        this.voucher_pin = voucher_pin;
        this.voucher_package = voucher_package;
        this.created_at = created_at;
        this.created_by = created_by;
    }
}
