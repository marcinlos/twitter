package pl.edu.agh.ed.twitter;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProcessor<T> extends SessionManager implements Job {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private int first = 0;
    private int firstVirt = 0;
    
    protected abstract Criterion fetchFilter();
    protected abstract int chunkSize();

    protected void beforeJob() {
        // empty
    } 

    protected void afterJob() {
        // empty
    }

    @Override
    public void run() {
        beforeJob();

        List<T> chunk = nextChunk();
        while (! chunk.isEmpty()) {
            logger.info("Chunk {}-{}", firstVirt - chunk.size(), firstVirt - 1);

            for (T item: chunk) {
                try {
                    process(item);
                } catch (Exception e) {
                    logger.error("During tweet processing", e);
                    logger.error("Skipping tweet");
                }
            }
            chunk = nextChunk();
        }
        afterJob();
        logger.info("No more chunks");
    }
    
    protected abstract void process(T item);
    
    protected void consumed() {
        -- first;
    }
    

    public List<T> nextChunk() {
        openSession();
        List<T> list = fetch(first, chunkSize(), fetchFilter());
        first += list.size();
        firstVirt += list.size();
        closeSession();
        return list;
    }
    
    protected abstract List<T> fetch(int first, int size, Criterion filter);
    
}
