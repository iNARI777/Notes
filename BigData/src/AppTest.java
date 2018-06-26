import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

public class AppTest 
{
    private static final String HDFS_PATH = "hdfs://192.168.85.131:9000/";

    private FileSystem fileSystem = null;
    private Configuration conf = null;

    @Before
    public void setUp() throws Exception{
        System.out.println("Start configurate the hadoop client...");
        conf = new Configuration();
        fileSystem = FileSystem.get(new URI(HDFS_PATH), conf, "root");
    }

    @After
    public void tearDown() {
        conf = null;
        fileSystem = null;
        System.out.println("Task over.");
    }

    /**
     * 创建文件
     * @throws Exception
     */
    @Test
    public void mkdir() throws Exception {
        fileSystem.mkdirs(new Path("/java/test"));
//        System.out.println(fileSystem);
    }

    /**
     * 创建文件，多次执行会覆盖原来的内容
     * @throws Exception
     */
    @Test
    public void create() throws Exception {
        FSDataOutputStream output = fileSystem.create(new Path("/java/test/hello.txt"));
        output.write("Hello Hadoop!\n".getBytes());
        output.flush();
        output.close();
    }

    /**
     * 读文件内容并输出到控制台
     */
    @Test
    public void cat() throws Exception {
        FSDataInputStream input = fileSystem.open(new Path("/java/test/hello.txt"));
        IOUtils.copyBytes(input, System.out, 1024);
        input.close();
    }

    /**
     * 重命名操作
     */
    @Test
    public void rename() throws Exception {
        String oldPath = "/java/test/hello.txt";
        String newPath = "/java/test/hadoop.txt";

        fileSystem.rename(new Path(oldPath), new Path(newPath));
    }

    /**
     * 从本地上传文件到HDFS
     */
    @Test
    public void put() throws Exception {
        String src = "/*.xml";
        String dst = "/java/test/core-site.xml";

        fileSystem.copyFromLocalFile(new Path(src), new Path(dst));
    }

    /**
     * 将HDFS中的文件拷贝到本地
     */
    @Test
    public void get() throws Exception {
        String src = "/java/test/hadoop.txt";
        String dst = "hahaha.txt";

        fileSystem.copyToLocalFile(new Path(src), new Path(dst));
    }

    /**
     * 列出HDFS下的文件状态。这里看到的副本系数是3，因为我们在hadoopclient中
     * 没设置副本系数，所以在执行create()的时候，使用的是默认的3.如果直接在HDFS
     * 中生成文件的话，其副本系数就是服务器中设置的副本系数1。
     */
    @Test
    public void listStatus() throws Exception{
        String path = "/java/test";
        FileStatus[] fs = fileSystem.listStatus(new Path(path));
        for (FileStatus f : fs) {
            if(!f.isDirectory()) {
                String fPath = f.getPath().toString();
                Short rep = f.getReplication();
                System.out.println(fPath + "\t" + rep);
            }
        }
    }
}
