package com.mrtlptkn.springbatchapp.listener;

import com.mrtlptkn.springbatchapp.sample02.Order;
import com.mrtlptkn.springbatchapp.sample02.RawOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


// Bizim bunu step sürecine dahil etmemiz gerekir.
// her atlatılan kayıt için bu sınıf çalışacak

@Component
@StepScope
public class RejectedLineListener implements SkipListener<RawOrder, Order> {

    private  static  final Logger log = LoggerFactory.getLogger(RejectedLineListener.class);
    private static  final  String HEADER = "raw_data,type,reason";
    // atlatılan verileri ise rejected_lines-2026-11-11.csv dosyasına ekletip orada arşivleyeceğiz. // raw_data,reason (raw_data) -> atlatılan veri, reason -> atlatılma sebebi


    @Value("${batch.output-dir}")
    private String outputDir;

    @Value("#{jobParameters['runDate']}")
    private String runDate;


    // FlatFileParseException durumları buraya düşer
    @Override
    public void onSkipInRead(Throwable t) {

        String rawData  = (t instanceof FlatFileParseException) ? ((FlatFileParseException) t).getInput() : "(veri bilinmiyor)";

        // FlatFileParse ederken alınan değer.
        write(rawData,"onSkipInRead",t);
        SkipListener.super.onSkipInRead(t);
    }

    // InvalidOrderException buraya düşer Bu 2 sınıf exception alınca kesinlikle bu atlatılan değerleri biryere yazdırmak lazım.
    @Override
    public void onSkipInProcess(RawOrder item, Throwable t) {
        write(item.toCsv(),"onSkipInProcess",t);
        SkipListener.super.onSkipInProcess(item, t);
    }

    // Veri kaynağına yazılırken bir exception (SQLException) bir unchecked oluşmuş olabilir. Bu durumda yazmak için elverişli olan bir kayıt, veritabanına yazarken commitleyemediğimizden kaybettik.
    @Override
    public void onSkipInWrite(Order item, Throwable t) {

        RawOrder r = new RawOrder(
                String.valueOf(item.orderId()),
                item.customer(),
                String.valueOf(item.amount()),
                item.orderDate().toString()
        );
        write(r.toCsv(),"onSkipInWrite",t);
        SkipListener.super.onSkipInWrite(item, t);

    }

    private void write(String rawData, String type, Throwable t){
        try{

            Path file = Path.of(outputDir + "/rejected_lines-" + runDate + ".csv");

            if(!Files.exists(file)){
                // dosyanın header değeri
                Files.writeString(file,HEADER + System.lineSeparator());
            }

            String line = rawData + "," + type + ", " + t.getMessage() + System.lineSeparator();
            Files.writeString(file,line, StandardOpenOption.APPEND);
            log.warn("Atlatılan satır: {} -> {}",rawData,t.getMessage());


        } catch (Exception e){
            log.error("Atlatılan satır yazılamadı: {} -> {}",rawData,t.getMessage());
        }
    }
}
