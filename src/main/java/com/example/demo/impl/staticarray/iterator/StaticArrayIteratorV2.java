package com.example.demo.impl.staticarray.iterator;

import com.example.demo.core.PrimitiveOrder;
import com.example.demo.impl.staticarray.orderlevel.OrderLevelV2;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.AbstractMap;

public class StaticArrayIteratorV2 implements Iterator<Map.Entry<Long, ? extends Collection<PrimitiveOrder>>> {

    // Tham chiếu đến mảng chứa các mức giá (level)
    private final OrderLevelV2[] levels;

    // Hướng duyệt: true cho Bid (giảm giá dần), false cho Ask (tăng giá dần)
    private final boolean isBid;

    // Giá tối thiểu đã được scaling
    private final long minPriceScaled;

    // Chỉ mục của mức giá hiện tại trong mảng levels
    private int currentIndex;

    // Biến để theo dõi chỉ mục của mức giá cuối cùng được trả về bởi next()
    private int lastReturnedLevelIndex = -1;

    public StaticArrayIteratorV2(OrderLevelV2[] levels, boolean isBid, long minPriceScaled) {
        this.levels = levels;
        this.isBid = isBid;
        this.minPriceScaled = minPriceScaled;

        // Khởi tạo chỉ mục ban đầu: cuối mảng cho Bid, đầu mảng cho Ask
        this.currentIndex = isBid ? levels.length - 1 : 0;
    }

    private long getScaledPrice(int index) {
        return index + minPriceScaled;
    }

    @Override
    public boolean hasNext() {
        // Tìm kiếm mức giá không rỗng tiếp theo để xem còn phần tử nào để duyệt không
        int tempIndex = currentIndex;
        while (tempIndex >= 0 && tempIndex < levels.length) {
            OrderLevelV2 level = levels[tempIndex];
            if (level != null && !level.isEmpty()) {
                return true;
            }
            tempIndex += isBid ? -1 : 1;
        }
        return false;
    }

    @Override
    public Map.Entry<Long, ? extends Collection<PrimitiveOrder>> next() {
        // Kiểm tra xem có phần tử tiếp theo không. Nếu không, ném ngoại lệ.
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // Tìm mức giá không rỗng tiếp theo (nếu cần)
        while (currentIndex >= 0 && currentIndex < levels.length) {
            OrderLevelV2 level = levels[currentIndex];
            if (level != null && !level.isEmpty()) {
                // Lưu lại chỉ mục của mức giá hiện tại trước khi trả về
                lastReturnedLevelIndex = currentIndex;

                long price = getScaledPrice(currentIndex);

                // Di chuyển tới mức giá tiếp theo cho lần gọi next() sau
                currentIndex += isBid ? -1 : 1;

                // Trả về một đối tượng Map.Entry mới chứa giá và mức giá
                return new AbstractMap.SimpleImmutableEntry<>(price, level);
            }
            // Di chuyển chỉ mục tới mức giá tiếp theo
            currentIndex += isBid ? -1 : 1;
        }

        // Trường hợp không mong muốn, nhưng vẫn cần ném ngoại lệ
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        // Kiểm tra xem next() đã được gọi chưa.
        if (lastReturnedLevelIndex == -1) {
            throw new IllegalStateException("next() has not been called or remove() has already been called for this element.");
        }

        // Đây là thay đổi quan trọng nhất: xóa toàn bộ mức giá khỏi mảng
        // Bằng cách gán null vào vị trí đã lưu.
        levels[lastReturnedLevelIndex] = null;

        // Reset biến để ngăn việc xóa lặp lại
        lastReturnedLevelIndex = -1;
    }
}