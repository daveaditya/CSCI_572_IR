package edu.usc.csci572;

import com.opencsv.bean.BeanField;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * @param <T>
 */
class CustomMappingStrategy<T> extends ColumnPositionMappingStrategy<T> {

    private boolean useHeader = true;

    public CustomMappingStrategy() {
    }

    public CustomMappingStrategy(boolean useHeader) {
        this.useHeader = useHeader;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.opencsv.bean.ColumnPositionMappingStrategy#generateHeader(java.lang.
     * Object)
     */
    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        final int numColumns = getFieldMap().values().size();

        if (numColumns == -1) {
            return super.generateHeader(bean);
        }

        String[] header = new String[numColumns];
        super.setColumnMapping(header);

        if (!useHeader) {
            return header;
        }

        BeanField<T, Integer> beanField;
        for (int i = 0; i < numColumns; i++) {
            beanField = findField(i);
            String columnHeaderName = beanField.getField().getDeclaredAnnotation(CsvBindByName.class).column();
            header[i] = columnHeaderName;
        }

        return header;
    }
}
