package BananaFructa.somnium.pyinterpreter;

import BananaFructa.somnium.Config;
import BananaFructa.somnium.pyinterpreter.objects.*;
import BananaFructa.somnium.pyinterpreter.objects.Python_Number;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ShadowedPythonCode {

    public int loopLimit;

    public String originalCode;

    public Definitions groups;
    public HashMap<String, Function2<ShadowedPythonCode, Python_Object[], Python_Object>> linkedFunctions = new HashMap<>();
    public HashMap<String, Function2<ShadowedPythonCode, Python_Object[], Python_Object>> linkedOperators = new HashMap<>();
    private Stack<Scope> scopeStack = new Stack<>();
    Scope globalScope;

    public ShadowedPythonCode(String originalCode, Definitions groups) {
        this.groups = groups;
        linkOperator("+",DefaultDefinitions::add);
        linkOperator("-",DefaultDefinitions::sub);
        linkOperator("*",DefaultDefinitions::mul);
        linkOperator("/",DefaultDefinitions::div);
        linkOperator("%",DefaultDefinitions::mod);
        linkOperator("==",DefaultDefinitions::equals);
        linkOperator("!=",DefaultDefinitions::notEquals);
        linkOperator("<",DefaultDefinitions::lesser);
        linkOperator(">",DefaultDefinitions::greater);
        linkOperator("<=",DefaultDefinitions::lesserOrEqual);
        linkOperator(">=",DefaultDefinitions::greaterOrEqual);
        linkFunction("print",DefaultDefinitions::print);
        linkOperator("not",DefaultDefinitions::not);
        linkOperator("and",DefaultDefinitions::and);
        linkOperator("or",DefaultDefinitions::or);
        linkOperator("in",DefaultDefinitions::in);
        linkFunction("List#append",DefaultDefinitions::append);
        linkFunction("List#clear",DefaultDefinitions::clear);
        linkFunction("List#copy",DefaultDefinitions::copy);
        linkFunction("List#count",DefaultDefinitions::count);
        linkFunction("List#extend",DefaultDefinitions::extend);
        linkFunction("List#index",DefaultDefinitions::index);
        linkFunction("List#insert",DefaultDefinitions::insert);
        linkFunction("List#pop",DefaultDefinitions::pop);
        linkFunction("List#remove",DefaultDefinitions::remove);
        linkFunction("List#reverse",DefaultDefinitions::reverse);
        linkFunction("List#sort",DefaultDefinitions::sort);
        linkOperator("**",DefaultDefinitions::pow);
        linkFunction("int",DefaultDefinitions::toInt);
        linkFunction("range",DefaultDefinitions::range);
        linkFunction("len",DefaultDefinitions::len);
        linkFunction("abs",DefaultDefinitions::abs);
        linkFunction("ceil",DefaultDefinitions::ceil);
        linkFunction("fabs",DefaultDefinitions::fabs);
        linkFunction("floor",DefaultDefinitions::floor);
        linkFunction("fma",DefaultDefinitions::fma);
        linkFunction("fmod",DefaultDefinitions::fmod);
        linkFunction("modf",DefaultDefinitions::modf);
        linkFunction("remainder",DefaultDefinitions::remainder);
        linkFunction("trunc",DefaultDefinitions::trunc);
        linkFunction("copysign",DefaultDefinitions::copysign);
        linkFunction("isfinite",DefaultDefinitions::isfinite);
        linkFunction("isinf",DefaultDefinitions::isinf);
        linkFunction("isnan",DefaultDefinitions::isnan);
        linkFunction("cbrt",DefaultDefinitions::cbrt);
        linkFunction("exp",DefaultDefinitions::exp);
        linkFunction("exp2",DefaultDefinitions::exp2);
        linkFunction("expm1",DefaultDefinitions::expm1);
        linkFunction("log",DefaultDefinitions::log);
        linkFunction("log2",DefaultDefinitions::log2);
        linkFunction("log10",DefaultDefinitions::log10);
        linkFunction("sqrt",DefaultDefinitions::sqrt);
        linkFunction("dist",DefaultDefinitions::dist);
        linkFunction("fsum",DefaultDefinitions::fsum);
        linkFunction("hypot",DefaultDefinitions::hypot);
        linkFunction("prod",DefaultDefinitions::prod);
        linkFunction("sumprod",DefaultDefinitions::sumprod);
        linkFunction("degrees",DefaultDefinitions::degrees);
        linkFunction("radians",DefaultDefinitions::radians);
        linkFunction("acos",DefaultDefinitions::acos);
        linkFunction("asin",DefaultDefinitions::asin);
        linkFunction("atan",DefaultDefinitions::atan);
        linkFunction("atan2",DefaultDefinitions::atan2);
        linkFunction("cos",DefaultDefinitions::cos);
        linkFunction("sin",DefaultDefinitions::sin);
        linkFunction("tan",DefaultDefinitions::tan);


        this.originalCode = originalCode;
        loopLimit = Config.pythonLoopLimit;
    }

    public void clearStack() {
        scopeStack.clear();
    }

    public void declareDefaults() {
        globalScope.registerVar("False",new Python_Bool(false));
        globalScope.registerVar("True",new Python_Bool(true));
        globalScope.registerVar("pi",new Python_Number(3.141592f));
        globalScope.registerVar("None", Python_NoneType.None);
    }

    public boolean isFunction(String s) {
        return groups.containsKey(s) || linkedFunctions.containsKey(s);
    }

    public void linkFunction(String name, Function2<ShadowedPythonCode, Python_Object[], Python_Object> func) {
        linkedFunctions.put(name,func);
    }

    public void linkOperator(String name, Function2<ShadowedPythonCode, Python_Object[], Python_Object> func) {
        linkedOperators.put(name,func);
    }

    public void execute() {
        clearStack(); // In case it threw an exception previously and the stack is still there
        execute(JavaPythonShadower.globalCodeFunction, new ArrayList<>(),true,false);
    }

    public boolean variableExists(String name) {
        boolean prevFunc = false;
        for (int i = scopeStack.size() - 1; i > -1;i--) {
            if (prevFunc && scopeStack.get(i).isFunctionScope) break;
            if (scopeStack.get(i).hasVar(name)) return true;
            prevFunc = scopeStack.get(i).isFunctionScope;
        }
        if (globalScope.hasVar(name)) return true;
        return false;
    }

    public Python_Object getVar(String name) {
        boolean prevFunc = false;
        for (int i = scopeStack.size() - 1; i > -1;i--) {
            if (prevFunc && scopeStack.get(i).isFunctionScope) break;
            if (scopeStack.get(i).hasVar(name)) return scopeStack.get(i).getVar(name);
            prevFunc = scopeStack.get(i).isFunctionScope;
        }
        if (globalScope.hasVar(name)) return globalScope.getVar(name);
        return null;
    }

    public void registerVariable(String name, Python_Object type) {
        if (variableExists(name)) {
            boolean prevFunc = false;
            for (int i = scopeStack.size() - 1; i > -1;i--) {
                if (prevFunc && scopeStack.get(i).isFunctionScope) return;
                if (scopeStack.get(i).hasVar(name)) scopeStack.get(i).registerVar(name,type);
                prevFunc = scopeStack.get(i).isFunctionScope;
            }
            if (globalScope.hasVar(name)) globalScope.registerVar(name,type);
        } else {
            scopeStack.peek().registerVar(name,type);
        }
    }

    boolean loopBroken = false;

    public Python_Object executeFunction(String functionName) {
        clearStack(); // In case it threw an exception previously and the stack is still there
        // Simulates as if the python code starts with
        // target_function()
        // at the beginning of the global scope and that only

        scopeStack.push(new Scope(true,false));
        globalScope = scopeStack.peek();
        declareDefaults();
        Python_Object ret = execute(functionName,new ArrayList<>(),true,false);
        scopeStack.pop();
        return ret;
    }

    // this will need a return type
    public Python_Object execute(String functionName, List<Tuple<String, Python_Object>> parameters, boolean function, boolean loop) {
        scopeStack.push(new Scope(function,loop));
        if (functionName.equals(JavaPythonShadower.globalCodeFunction)) globalScope = scopeStack.peek();
        declareDefaults();
        for (Tuple<String, Python_Object> param : parameters) scopeStack.peek().registerVar(param.getA(),param.getB());
        ExpressionGroup group = groups.get(functionName);
        List<ASTNode> expressions = group.expressions;
        for (ASTNode e : expressions) {
            Python_Object o = evaluateExpression(e);
            if (scopeStack.peek().returnFlag) {
                scopeStack.pop();
                return o;
            }
        }
        scopeStack.pop();
        return Python_NoneType.None;
    }

    public void setVariable(ASTNode variable, Python_Object rightEval) {
        if (variable instanceof ASTListAccessor) { // Assigning to a list access ughhh
            ASTListAccessor accessor = (ASTListAccessor) variable;
            if (accessor.range) throw new RuntimeException("Runtime LID Python: Cannot assign to a range.");
            Python_List list = (Python_List)evaluateExpression(accessor.children.get(0));
            Python_Number index = (Python_Number) evaluateExpression(accessor.first);
            if (index.mode != NumberMode.INT) throw new RuntimeException("Runtime LID Python: List index must be an integer.");
            if (index.i >= 0) {
                if (list.elements.size() <= index.i) throw new RuntimeException("Runtime LID Pythin: Index out of range.");
                list.elements.set(index.i,rightEval);
            } else {
                if (list.elements.size() < -index.i) throw new RuntimeException("Runtime LID Pythin: Index out of range.");
                list.elements.set(list.elements.size() + index.i,rightEval);
            }
        } else {
            String leftVarName = variable.name;
            registerVariable(leftVarName, rightEval);
        }
    }

    public Python_Object evaluateExpression(ASTNode node) {

        if (node instanceof ASTOperator && node.name.equals("=")) {
            Python_Object rightEval = evaluateExpression(node.children.get(1));
            if (node.children.get(0) instanceof ASTTuple) {
                if (!(rightEval instanceof Python_Tuple)) throw new RuntimeException("Runtime LID Pythin: Only a tuple can be unpacked.");
                List<ASTNode> leftTuple = ((ASTTuple) node.children.get(0)).elements;
                List<Python_Object> rightTuple = ((Python_Tuple) rightEval).elements;
                if (leftTuple.size() != rightTuple.size()) throw new RuntimeException("Runtime LID Pythin: Tuples must be the same size for unpacking.");
                for (int i = 0;i < leftTuple.size();i++) { // unpacking it as multiple a = b statements
                    setVariable(leftTuple.get(i),rightTuple.get(i));
                }
            } else setVariable(node.children.get(0),rightEval);
            return Python_NoneType.None;
        } else {
            if (node instanceof ASTOperator) {
                if (node.name.equals(".")) {
                    if (node.children.size() != 2)
                        throw new RuntimeException("Runtime LID Python: Hanging attribute access operator.");
                    if (!(node.children.get(1) instanceof ASTFunction))
                        throw new RuntimeException("Runtime LID Python: Field attributes not implemented.");
                    ASTNode object = node.children.get(0);
                    ASTNode attribute = (ASTFunction) node.children.get(1);
                    Python_Object first = evaluateExpression(object);
                    ASTFunction function = new ASTFunction(Python_Object.getTypeName(first) + "#" + attribute.name);
                    function.children.add(object);
                    function.children.addAll(attribute.children);
                    return evaluateExpression(function); // (self,...) evaluation, does this work with fields??? idk don't think i will need them
                }
            }
            List<Python_Object> children = new ArrayList<>();
            for (ASTNode c : node.children) {
                children.add(evaluateExpression(c));
            }
            if (node instanceof ASTNumber) {
                return ((ASTNumber) node).value;
            }
            if (node instanceof ASTLambda lambda) {
                return new Python_Function(lambda.internalFunctionName);
            }
            if (node instanceof ASTOperator) {
                if (node instanceof ASTListAccessor) {
                    ASTNode first = ((ASTListAccessor) node).first;
                    ASTNode second = ((ASTListAccessor) node).second;
                    boolean range = ((ASTListAccessor) node).range;
                    if (!range) return DefaultDefinitions.listAccess(children.get(0), evaluateExpression(first));
                    else {
                        return DefaultDefinitions.listAccess(children.get(0), first == null ? null : evaluateExpression(first), second == null ? null : evaluateExpression(second));
                    }
                } else return linkedOperators.get(node.name).apply(this, children.toArray(Python_Object[]::new));
            }
            if (node instanceof ASTVariable) {
                if (variableExists(node.name)) return getVar(node.name);
                else if (isFunction(node.name)) return new Python_Function(node.name);
                else throw new RuntimeException("Runtime LID Python: No such variable with the name " + node.name + ".");
            }
            if (node instanceof ASTFunction) {
                if (groups.containsKey(node.name)) {
                    FunctionDefinition def = (FunctionDefinition) groups.get(node.name);
                    List<Tuple<String, Python_Object>> params = new ArrayList<>();
                    for (int i = 0;i < def.parameters.size();i++) {
                        params.add(new Tuple<>(def.parameters.get(i),children.get(i)));
                    }
                    return execute(node.name,params,true,false);
                }
                if (linkedFunctions.containsKey(node.name)) {
                    Function2<ShadowedPythonCode, Python_Object[], Python_Object> func = linkedFunctions.get(node.name);
                    return func.apply(this, children.toArray(Python_Object[]::new));
                }
                if (variableExists(node.name)) {
                    Python_Object var = getVar(node.name);
                    if (var instanceof Python_Function) {
                        String funcName = ((Python_Function) var).name;
                        FunctionDefinition def = (FunctionDefinition) groups.get(funcName);
                        List<Tuple<String, Python_Object>> params = new ArrayList<>();
                        for (int i = 0;i < def.parameters.size();i++) {
                            params.add(new Tuple<>(def.parameters.get(i),children.get(i)));
                        }
                        return execute(funcName,params,true,false);
                    }
                }
                throw new RuntimeException("Runtime LID Python: No such function with the name " + node.name + ".");
            }
            if (node instanceof ASTReturn) {
                for (int i = scopeStack.size() - 1;i > -1;i--) {
                    scopeStack.get(i).returnFlag = true;
                    if (scopeStack.get(i).isFunctionScope) break;
                }

                if (((ASTReturn)node).returnExp == null) return Python_NoneType.None;
                return evaluateExpression(((ASTReturn)node).returnExp); // This is different as it
            }
            if (node instanceof ASTBreak) {
                for (int i = scopeStack.size() - 1;i > -1;i--) {
                    if (scopeStack.get(i).isLoopScope) {
                        scopeStack.get(i).returnFlag = true;
                        loopBroken = true; // YUCKY
                        return Python_NoneType.None;
                    } else if (scopeStack.get(i).isFunctionScope) throw new RuntimeException("Runtime LID Python: Break statement reached bu not inside any loop.");
                    else scopeStack.get(i).returnFlag = true;
                }
                throw new RuntimeException("Runtime LID Python: Break statement reached bu not inside any loop.");
            }
            if (node instanceof ASTSwitch) {
                ASTNode conditions = ((ASTSwitch)node).conditionExpression;
                Python_Object res = evaluateExpression(conditions);
                if (!(res instanceof Python_Bool)) res = new Python_Bool(res);
                if (((Python_Bool)res).b) {
                    return execute(((ASTSwitch)node).trueScope,new ArrayList<>(), false, false);
                } else {
                    for (Tuple<ASTNode,String> elif : ((ASTSwitch)node).elifs) {
                        Python_Object elifRes = evaluateExpression(elif.getA());
                        if (!(elifRes instanceof Python_Bool)) res = new Python_Bool(res);
                        if (((Python_Bool)elifRes).b) {
                            return execute(elif.getB(),new ArrayList<>(), false, false);
                        }
                    }
                    if (((ASTSwitch)node).falseScope != null) return execute(((ASTSwitch)node).falseScope,new ArrayList<>(), false, false);
                }
            }
            if (node instanceof ASTList) {
                List<ASTNode> nodes = ((ASTList) node).elements;
                List<Python_Object> evals = new ArrayList<>();
                for (ASTNode e : nodes) evals.add(evaluateExpression(e));
                Python_List l = new Python_List();
                l.elements = evals;
                return l;
            }
            if (node instanceof ASTTuple) {
                List<ASTNode> nodes = ((ASTTuple) node).elements;
                List<Python_Object> evals = new ArrayList<>();
                for (ASTNode e : nodes) evals.add(evaluateExpression(e));
                Python_Tuple l = new Python_Tuple();
                l.elements = evals;
                return l;
            }
            if (node instanceof ASTFor) {
                ASTFor f = (ASTFor) node;
                Python_Object listObj = evaluateExpression(f.list);
                if (!(listObj instanceof Python_List)) throw new RuntimeException("Runtime LID Python: Expected to iterate over list.");
                Python_List list = (Python_List) listObj;
                int loop = 0;
                for (Python_Object o : list.elements) {
                    if (loop == loopLimit) break;
                    List<Tuple<String, Python_Object>> interationDefinitions = new ArrayList<>();
                    interationDefinitions.add(new Tuple<>(f.var,o));
                    Python_Object result = execute(f.scope,interationDefinitions,false, true);
                    if (scopeStack.peek().returnFlag) return result;
                    if (loopBroken) {
                        loopBroken = false;
                        break; // This is why i called it shadowing
                    }
                    loop++;
                }
            }
            if (node instanceof ASTWhile) {
                ASTWhile astWhile = (ASTWhile) node;
                Python_Object boolObj = evaluateExpression(astWhile.statement);
                Python_Bool b;
                if (!(boolObj instanceof Python_Bool)) b = new Python_Bool(boolObj);
                else b = (Python_Bool) boolObj;
                int loop = 0;
                while (b.b) {
                    if (loop == loopLimit) break;
                    Python_Object result = execute(astWhile.scope,new ArrayList<>(),false,true);
                    if (scopeStack.peek().returnFlag) return result;
                    if (loopBroken) {
                        loopBroken = false;
                        break;
                    }
                    boolObj = evaluateExpression(astWhile.statement);
                    if (!(boolObj instanceof Python_Bool)) b = new Python_Bool(boolObj);
                    else b = (Python_Bool) boolObj;
                    loop++;
                }

            }
            if (node instanceof ASTConditional) {
                ASTConditional conditional = (ASTConditional)node;
                ASTNode condition = conditional.condition;
                Python_Object res = evaluateExpression(condition);
                if (!(res instanceof Python_Bool)) throw new RuntimeException("Runtime LID Python: Not a boolean expression.");
                if (((Python_Bool) res).b) {
                    return evaluateExpression(conditional.trueValue);
                } else {
                    return evaluateExpression(conditional.falseValue);
                }
            }
            if (node instanceof ASTString) {
                return new Python_String(node.name);
            }
            return Python_NoneType.None;
        }
    }

}
