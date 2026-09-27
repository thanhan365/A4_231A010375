package vn.edu.vhu.ltdd.a4events;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "A4_231A010375";

    private EditText edtSoA, edtSoB, edtCanNang, edtChieuCao;
    private TextView tvKetQua, tvBmi, tvPhanLoai, tvLichSu;
    private ArrayList<String> dsLichSu = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // ---- Ánh xạ view ----
        edtSoA = findViewById(R.id.edtSoA);
        edtSoB = findViewById(R.id.edtSoB);
        tvKetQua = findViewById(R.id.tvKetQua);
        tvLichSu = findViewById(R.id.tvLichSu);
        edtCanNang = findViewById(R.id.edtCanNang);
        edtChieuCao = findViewById(R.id.edtChieuCao);
        tvBmi = findViewById(R.id.tvBmi);
        tvPhanLoai = findViewById(R.id.tvPhanLoai);

        Button btnCong = findViewById(R.id.btnCong);
        Button btnTru = findViewById(R.id.btnTru);
        Button btnNhan = findViewById(R.id.btnNhan);
        Button btnChia = findViewById(R.id.btnChia);
        Button btnXoa = findViewById(R.id.btnXoa);
        Button btnTinhBmi = findViewById(R.id.btnTinhBmi);

        // ---- Khôi phục lịch sử nếu xoay màn hình (NC2) ----
        if (savedInstanceState != null) {
            ArrayList<String> savedList = savedInstanceState.getStringArrayList("DS_LICH_SU");
            if (savedList != null) {
                dsLichSu = savedList;
                hienThiLichSu();
            }
        }

        // ---- Cách 1: mỗi nút một listener bằng biểu thức lambda ----
        btnCong.setOnClickListener(v -> tinhToan('+'));
        btnTru.setOnClickListener(v -> tinhToan('-'));

        // ---- Cách 2: một listener dùng chung, phân biệt bằng id ----
        View.OnClickListener chung = v -> {
            int id = v.getId();
            if (id == R.id.btnNhan) {
                tinhToan('*');
            } else if (id == R.id.btnChia) {
                tinhToan('/');
            }
        };
        btnNhan.setOnClickListener(chung);
        btnChia.setOnClickListener(chung);

        btnXoa.setOnClickListener(v -> xoaTrang());
        btnTinhBmi.setOnClickListener(v -> tinhBmi());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Lưu danh sách lịch sử khi xoay màn hình (NC2)
        outState.putStringArrayList("DS_LICH_SU", dsLichSu);
    }

    // =============== MÁY TÍNH ===============

    private void tinhToan(char phepToan) {
        String chuoiA = edtSoA.getText().toString().trim();
        String chuoiB = edtSoB.getText().toString().trim();

        // Bước 1: kiểm tra rỗng và báo lỗi ngay trên ô nhập
        if (chuoiA.isEmpty()) {
            edtSoA.setError(getString(R.string.err_empty));
            edtSoA.requestFocus();
            return;
        }
        if (chuoiB.isEmpty()) {
            edtSoB.setError(getString(R.string.err_empty));
            edtSoB.requestFocus();
            return;
        }

        // Bước 2: chuyển chuỗi sang số, bẫy lỗi định dạng
        double a, b;
        try {
            a = Double.parseDouble(chuoiA);
            b = Double.parseDouble(chuoiB);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Dữ liệu nhập không phải số: '" + chuoiA + "', '" + chuoiB + "'", e);
            Toast.makeText(this, R.string.err_not_number, Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 3: xử lý trường hợp đặc biệt
        if (phepToan == '/' && b == 0) {
            edtSoB.setError(getString(R.string.err_divide_zero));
            Toast.makeText(this, R.string.err_divide_zero, Toast.LENGTH_SHORT).show();
            return;
        }

        double ketQua;
        switch (phepToan) {
            case '+': ketQua = a + b; break;
            case '-': ketQua = a - b; break;
            case '*': ketQua = a * b; break;
            default:  ketQua = a / b; break;
        }

        String strResult = String.format(Locale.getDefault(), "%.2f %c %.2f = %.2f",
                a, phepToan, b, ketQua);
        tvKetQua.setText(strResult);
        Log.d(TAG, "Phép tính: " + strResult);

        // NC2: Lưu lịch sử 5 phép tính gần nhất
        luuLichSu(strResult);
    }

    private void luuLichSu(String chuoiPhepTinh) {
        dsLichSu.add(0, chuoiPhepTinh);
        if (dsLichSu.size() > 5) {
            dsLichSu.remove(dsLichSu.size() - 1);
        }
        hienThiLichSu();
    }

    private void hienThiLichSu() {
        if (dsLichSu.isEmpty()) {
            tvLichSu.setText("Lịch sử: (Trống)");
            return;
        }
        StringBuilder sb = new StringBuilder("Lịch sử 5 phép tính gần nhất:\n");
        for (int i = 0; i < dsLichSu.size(); i++) {
            sb.append(i + 1).append(". ").append(dsLichSu.get(i)).append("\n");
        }
        tvLichSu.setText(sb.toString().trim());
    }

    private void xoaTrang() {
        edtSoA.setText("");
        edtSoB.setText("");
        edtSoA.setError(null);
        edtSoB.setError(null);
        tvKetQua.setText(R.string.result_placeholder);
        edtSoA.requestFocus();
    }

    // =============== BMI ===============

    private void tinhBmi() {
        try {
            double canNang = Double.parseDouble(edtCanNang.getText().toString().trim());
            double chieuCao = Double.parseDouble(edtChieuCao.getText().toString().trim());

            if (canNang <= 0 || chieuCao <= 0) {
                Toast.makeText(this, R.string.err_positive, Toast.LENGTH_SHORT).show();
                return;
            }
            // Cho phép nhập 1.70 (mét) hoặc 170 (cm)
            if (chieuCao > 3) {
                chieuCao = chieuCao / 100.0;
            }

            double bmi = canNang / (chieuCao * chieuCao);
            tvBmi.setText(String.format(Locale.getDefault(), "BMI = %.1f", bmi));
            
            // NC3: Hiển thị phân loại và đổi màu kết quả
            phanLoaiVaDoiMau(bmi);

        } catch (NumberFormatException e) {
            Log.e(TAG, "Lỗi nhập liệu BMI", e);
            Toast.makeText(this, R.string.err_not_number, Toast.LENGTH_SHORT).show();
        }
    }

    /** NC3: Đổi màu dòng kết quả BMI theo mức: xanh (bình thường), vàng (thừa cân), đỏ (béo phì). */
    private void phanLoaiVaDoiMau(double bmi) {
        if (bmi < 18.5) {
            tvPhanLoai.setText(getString(R.string.bmi_under));
            tvPhanLoai.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        } else if (bmi < 23) {
            tvPhanLoai.setText(getString(R.string.bmi_normal));
            tvPhanLoai.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else if (bmi < 25) {
            tvPhanLoai.setText(getString(R.string.bmi_over));
            tvPhanLoai.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
        } else {
            tvPhanLoai.setText(getString(R.string.bmi_obese));
            tvPhanLoai.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        }
    }
}