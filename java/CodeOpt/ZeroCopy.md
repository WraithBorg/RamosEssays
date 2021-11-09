## 零拷贝文件存储
```
    许多Web应用提供大量的静态内容，主要就是从磁盘读取数据然后将数据写回套接字，中间不涉及数据的变换。
这种操作对CPU的使用相对较少，但是效率很低：
首先，内核从文件读取数据，然后将数据从内核空间拷贝到用户进程空间，最后应用程序将数据拷贝回内核空间并通过套接字发送。
实际上，在整个流程中应用程序仅充当一个将数据从磁盘拷贝到套接字的低效中间层。
    每次数据跨越用户态和内核态的边界，数据都需要拷贝，拷贝操作消耗CPU和内存带宽。
幸运的是通过一种称为“零拷贝”的技术可消除这些不必要的拷贝。使用零拷贝的应用要求内核将磁盘数据直接拷贝到套接字而不再经过应用。
零拷贝可以极大的提高应用的性能并减少上下文在内核态和用户态之间的切换次数。
    基于 Sendfile 系统调用的零拷贝方式，整个拷贝过程会发生 2 次上下文切换，1 次 CPU 拷贝和 2 次 DMA 拷贝。
用户程序读写数据的流程如下：
    用户进程通过 sendfile() 函数向内核（kernel）发起系统调用，上下文从用户态（user space）切换为内核态（kernel space）。
    CPU 利用 DMA 控制器将数据从主存或硬盘拷贝到内核空间（kernel space）的读缓冲区（read buffer）。
    CPU 将读缓冲区（read buffer）中的数据拷贝到的网络缓冲区（socket buffer）。
    CPU 利用 DMA 控制器将数据从网络缓冲区（socket buffer）拷贝到网卡进行数据传输。
    上下文从内核态（kernel space）切换回用户态（user space），Sendfile 系统调用执行返回。
```
### 零拷贝java示例代码
```
// 应用场景: Kafka,Spark,Netty,Nginx,Spring WebFlux
// RocketMq 在消费消息时使用了mmap,kafka使用了sendFile

@GetMapping("download3/{filename}")
public Mono<Void> download (ServerHttpResponse response,@PathVariable String filename) {
    ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
    response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
    response.getHeaders().setContentType(MediaType.APPLICATION_PDF);
    File file = new File(filePath + filename);
    return zeroCopyResponse.writeWith(file, 0, file.length());
}
```
```
需求一,客户点击导出Excel按钮,下载Excel
难点,Excel文件很大,无论是生成Excel还是下载Excel都会影响CPU和内存

自认为最完美的解决方案:
1:生成Excel
先在本地创建Excel,然程序生成数据写入到Excel,
但是,不能一次性把所有数据都写入到Excel,要分批写入,
每当内存中的数据超过20MB时(视具体情况而定,瞎写的),
将内存中的数据写入到Excel中,然后清空内存数据,生成下一批数据,
2:下载Excel
此时服务器充当的就是一个Nginx的角色,一般来说就是读取文件流并将文件流返回给浏览器,
但是完全不必这样做,因为这样会影响CPU和内存,
可以采用nginx的方式,利用零拷贝原理下载文件,
即CPU利用DMA控制器将数据从网络缓冲区拷贝到网卡进行数据传输

利用easyexcel可以实现上述生成excel的功能,
至于零拷贝下载文件,java有现成的方法
ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
zeroCopyResponse.writeWith(file, 0, file.length());
```
