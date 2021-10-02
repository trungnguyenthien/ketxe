# Hướng dẫn test app

## Nội dung test

* Ứng dụng có hiển thị đúng toạ độ đã lưu không?
* Ứng dụng có lấy data cho đúng toạ độ không?
* Ứng dụng có hiển thị điểm kẹt xe như data trả về không?

Lưu ý:

* KHÔNG TEST: Tại hiện trường (ngoài đường) có kẹt xe như trong app đã thông báo hay không!? Vì chức năng app chỉ là hiển thị data chứ không phải thu thập data.
* KHÔNG TEST: Địa điểm kẹt xe có trừng khớp với các bản đồ, ứng dụng khác không!? Mỗi bản đồ, ứng dụng có cách lấy data khác nhau nên không đảm bảo địa điểm kẹt xe sẽ giống nhau. Thậm chí, nếu so với BingMap (cùng nguồn gốc Microsoft), nhưng cũng không tài liệu nào giới thiệu nguồn data của BingMap lấy từ data nào, điều kiện lấy ra sao, nên việc so sánh cũng không đảm bảo sẽ giống nhau.



# Cách Test

Sử dụng chức năng debug để lấy được các thông tin sau của 1 toạ độ

Sample:

```
https://dev.virtualearth.net/REST/v1/Traffic/Incidents/-34.044679978001014,150.9703812164957,-33.95474781740914,151.04493860772305?severity=3%2C4&key=AoAJOXY4xxhJ0CUddHOJfpx9CRnQBWo5OmfS5A2OBexlD4OuRN6QdNeAiSrUB_Jk 


 location=lat/lng: (-33.99971389770508,151.00765991210938) 


 areaTopLeft=lat/lng: (-34.044679978001014,150.9703812164957) 


 BottomRight=lat/lng: (-33.95474781740914,151.04493860772305)
```



### Ứng dụng có hiển thị đúng toạ độ đã lưu không?

So sánh toạ độ trong ứng dụng với googleMap xem có khớp với nhau không

```
http://maps.google.com/maps?z=18&q=[LOCATION]
Ví dụ: http://maps.google.com/maps?z=18&q=10.843610763549805,106.79959106445312
```

### Ứng dụng có lấy data cho đúng toạ độ không?

Dùng cách tương tự để kiểm tra 3 điểm location, topLeft, BottomRight có bố cục thành vị trí (trên bản đồ) như hình dưới đây không

![image-20211002102848340](https://tva1.sinaimg.cn/large/008i3skNgy1gv0t2p5wnjj60p00jedgd02.jpg)

### Ứng dụng có hiển thị điểm kẹt xe như data trả về không?

Sử dụng ứng dụng Postman (https://www.postman.com/downloads/) để xem được data api trả về, và đối chiếu với các điểm kẹt xe trong ứng dụng Jamos.

![image-20211002103108894](https://tva1.sinaimg.cn/large/008i3skNgy1gv0t52hqtjj613y0u0q7k02.jpg)