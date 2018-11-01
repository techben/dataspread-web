package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.QueryOptimization.FormulaExecutor;

import java.util.*;

public class SingleTransformOperator extends TransformOperator {

    private List<ScalarConstantPtg> literials;

    public SingleTransformOperator(Ptg ptg) throws OptimizationError {
        super();
        if (!(ptg instanceof ScalarConstantPtg)){
            throw OptimizationError.UNSUPPORTED_CASE;
        }
        literials = Arrays.asList(new ScalarConstantPtg[]{(ScalarConstantPtg)ptg});
        ptgs = new Ptg[] {new ConstantVariablePtg(0)};


    }

    public SingleTransformOperator(LogicalOperator[] operators, Ptg ptg) throws OptimizationError {
        super();
        ptgs = new Ptg[getPtgSize(operators) + 1];
        literials = new ArrayList<>();
        Map<LogicalOperator,Integer> operatorId = new TreeMap<>();
        int cursor = 0;
        for (LogicalOperator op:operators){
            if (op instanceof DataOperator || op instanceof AggregateOperator){
                if (!operatorId.containsKey(op)){
                    operatorId.put(op,inEdges.size());
                    connect(op,this);
                }
                ptgs[cursor++] = new RefVariablePtg(operatorId.get(op));
                continue;
            }
            if (op instanceof SingleTransformOperator){
                SingleTransformOperator transform = (SingleTransformOperator)op;
                int[] newRefId = new int[transform.getInEdges().size()];

                for (int i = 0,isize = transform.getInEdges().size(); i < isize; i++){
                    LogicalOperator o = transform.getInEdges().get(i).getInVertex();
                    if (operatorId.containsKey(o)){
                        newRefId[i] = operatorId.get(o);
                    }
                    else {
                        operatorId.put(o,inEdges.size());
                        newRefId[i] = inEdges.size();
                        connect(o,this);
                    }
                }

                for (int i = 0; i < transform.ptgs.length; i++){
                    Ptg p =transform.ptgs[i];
                    ptgs[cursor++] = p;
                    if (!(p instanceof VariablePtg))
                        continue;
                    VariablePtg var = (VariablePtg)p;
                    if (var instanceof ConstantVariablePtg){
                        var.setIndex(var.getIndex() + literials.size());
                    }
                    else {
                        var.setIndex(newRefId[var.getIndex()]);
                    }
                }

                literials.addAll(transform.literials);

                continue;
            }
            throw OptimizationError.UNSUPPORTED_CASE;
        }



    }

    @Override
    public void evaluate(FormulaExecutor context) throws OptimizationError {
        for (Edge e: inEdges)
            if (!e.resultIsReady())
                return;

        Ptg[] data = new Ptg[inEdges.size()];

        for (int i = 0, isize = inEdges.size(); i < isize;i++){
            Object result = inEdges.get(i).popResult().get(0);
            if (result instanceof Double)
                data[i] = new NumberPtg((Double)result);
            else if (result instanceof String)
                data[i] = new StringPtg((String) result);
            else
                throw OptimizationError.UNSUPPORTED_TYPE;
        }

        Ptg[] ptgs = Arrays.copyOf(this.ptgs, this.ptgs.length);
        for (int i = 0, isize = ptgs.length; i < isize;i++){
            if (ptgs[i] instanceof ConstantVariablePtg){
                ptgs[i] = literials.get(((VariablePtg)ptgs[i]).getIndex());
            }
            else if (ptgs[i] instanceof  RefVariablePtg){
                ptgs[i] = data[((VariablePtg)ptgs[i]).getIndex()];
            }
        }

        List result = Arrays.asList(new Object[]{evaluate(ptgs)});

        for (Edge o:outEdges){
            o.setResult(result);
        }


        _evaluated = true;
    }

    @Override
    public void merge(DataOperator TransformOperator) throws OptimizationError {
        throw OptimizationError.UNSUPPORTED_FUNCTION;
    }

    @Override
    public int returnSize() {
        return 1;
    }

    private int getPtgSize(LogicalOperator[] operators) throws OptimizationError {
        int size = 0;
        for (LogicalOperator op:operators){
            if (op instanceof DataOperator){
                if (((DataOperator) op).getRegion().getCellCount() > 1)
                    throw new OptimizationError("Multiple Values in Single Operator");
                size++;
                continue;
            }
            if (op instanceof AggregateOperator){
                size++;
                continue;
            }
            if (op instanceof SingleTransformOperator){
                size += ((SingleTransformOperator) op).ptgs.length;
                continue;
            }
            throw OptimizationError.UNSUPPORTED_CASE;
        }
        return size;
    }

}