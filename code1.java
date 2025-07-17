import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class PersonalTaskManagerRefactored {

    // Đường dẫn file JSON để lưu trữ danh sách nhiệm vụ
    private static final String DB_FILE_PATH = "tasks_database.json";

    // Định dạng ngày được sử dụng trong chương trình (yyyy-MM-dd)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Đọc danh sách nhiệm vụ từ file JSON.
     * Trả về một đối tượng JSONArray chứa các nhiệm vụ hiện có.
     * Nếu file không tồn tại hoặc lỗi xảy ra, trả về mảng rỗng.
     */
    private JSONArray loadTasks() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = parser.parse(reader);
            return (obj instanceof JSONArray) ? (JSONArray) obj : new JSONArray();
        } catch (IOException | ParseException e) {
            // Trả về mảng rỗng nếu không đọc được file
            return new JSONArray();
        }
    }

    /**
     * Ghi danh sách nhiệm vụ vào file JSON.
     * Ghi đè toàn bộ dữ liệu hiện tại trong file.
     */
    private void saveTasks(JSONArray tasks) {
        try (FileWriter writer = new FileWriter(DB_FILE_PATH)) {
            writer.write(tasks.toJSONString());
            writer.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu file: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu đầu vào trước khi thêm nhiệm vụ.
     * Gồm: kiểm tra tiêu đề không rỗng, ngày đúng định dạng, và mức độ ưu tiên hợp lệ.
     */
    private boolean isValidInput(String title, String dueDateStr, String priority) {
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Lỗi: Tiêu đề không được để trống.");
            return false;
        }
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            System.out.println("Lỗi: Ngày đến hạn không được để trống.");
            return false;
        }
        try {
            LocalDate.parse(dueDateStr, DATE_FORMATTER); // Kiểm tra định dạng ngày
        } catch (DateTimeParseException e) {
            System.out.println("Lỗi: Ngày không hợp lệ. Định dạng đúng: YYYY-MM-DD.");
            return false;
        }

        // Mức độ ưu tiên chỉ hợp lệ nếu là Thấp, Trung bình hoặc Cao (không phân biệt hoa thường)
        if (!priority.matches("(?i)Thấp|Trung bình|Cao")) {
            System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ. Chọn: Thấp, Trung bình hoặc Cao.");
            return false;
        }
        return true;
    }

    /**
     * Kiểm tra xem nhiệm vụ mới có bị trùng với nhiệm vụ đã có hay không.
     * Trùng nếu cùng tiêu đề (không phân biệt hoa thường) và cùng ngày đến hạn.
     */
    private boolean isDuplicate(JSONArray tasks, String title, String dueDateStr) {
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (task.get("title").toString().equalsIgnoreCase(title)
                    && task.get("due_date").toString().equals(dueDateStr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Thêm nhiệm vụ mới vào hệ thống nếu hợp lệ.
     * Gồm các thông tin: tiêu đề, mô tả, ngày đến hạn, mức độ ưu tiên.
     * Nếu thành công, nhiệm vụ được thêm vào file JSON.
     * Áp dụng các nguyên tắc KISS, DRY, YAGNI.
     */
    public JSONObject addTask(String title, String description, String dueDateStr, String priority) {
        // Kiểm tra dữ liệu đầu vào
        if (!isValidInput(title, dueDateStr, priority)) {
            return null;
        }

        // Đọc danh sách nhiệm vụ từ file
        JSONArray tasks = loadTasks();

        // Kiểm tra trùng lặp
        if (isDuplicate(tasks, title, dueDateStr)) {
            System.out.println("Nhiệm vụ đã tồn tại.");
            return null;
        }

        // Tạo nhiệm vụ mới dưới dạng JSONObject
        JSONObject newTask = new JSONObject();
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME); // Thời gian hiện tại

        // Gán các thông tin nhiệm vụ
        newTask.put("id", UUID.randomUUID().toString()); // ID duy nhất
        newTask.put("title", title);
        newTask.put("description", description);
        newTask.put("due_date", dueDateStr);
        newTask.put("priority", priority);
        newTask.put("status", "Chưa hoàn thành");
        newTask.put("created_at", now);
        newTask.put("last_updated_at", now);

        // Thêm vào danh sách và lưu lại vào file
        tasks.add(newTask);
        saveTasks(tasks);

        System.out.println("Nhiệm vụ đã được thêm thành công.");
        return newTask;
    }

    /**
     * Hàm main để chạy minh họa thêm nhiệm vụ.
     * Gồm: thêm hợp lệ, thêm trùng lặp và thêm không hợp lệ.
     */
    public static void main(String[] args) {
        PersonalTaskManagerRefactored manager = new PersonalTaskManagerRefactored();

        // Thêm nhiệm vụ hợp lệ
        manager.addTask("Học Java", "Ôn tập về JSON và xử lý file", "2025-07-20", "Cao");

        // Thử thêm trùng lặp (cùng tiêu đề và ngày)
        manager.addTask("Học Java", "Lặp lại nhiệm vụ cũ", "2025-07-20", "Cao");

        // Thử thêm nhiệm vụ không hợp lệ (tiêu đề rỗng)
        manager.addTask("", "Không có tiêu đề", "2025-07-22", "Thấp");
    }
}
