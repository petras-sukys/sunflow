package org.sunflow.core.primitive;

import org.sunflow.core.IntersectionState;
import org.sunflow.core.Primitive;
import org.sunflow.core.Ray;
import org.sunflow.core.Shader;
import org.sunflow.core.ShadingState;
import org.sunflow.math.OrthoNormalBasis;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;

public class Plane implements Primitive {
    private Point3 center;
    private Vector3 normal;
    private OrthoNormalBasis basis;
    private Shader shader;
    int k;
    private float bnu, bnv, bnd;
    private float cnu, cnv, cnd;

    public Plane(Shader shader, Point3 center, Vector3 normal) {
        this.center = center;
        this.normal = new Vector3(normal).normalize();
        this.shader = shader;
        basis = OrthoNormalBasis.makeFromW(this.normal);
        k = 3;
        bnu = bnv = bnd = 0;
        cnu = cnv = cnd = 0;
    }

    public Plane(Shader shader, Point3 a, Point3 b, Point3 c) {
        this.shader = shader;
        Point3 v0 = center = a;
        Point3 v1 = b;
        Point3 v2 = c;
        Vector3 ng = normal = Vector3.cross(Point3.sub(v1, v0, new Vector3()), Point3.sub(v2, v0, new Vector3()), new Vector3()).normalize();
        if (Math.abs(ng.x) > Math.abs(ng.y) && Math.abs(ng.x) > Math.abs(ng.z))
            k = 0;
        else if (Math.abs(ng.y) > Math.abs(ng.z))
            k = 1;
        else
            k = 2;
        float ax, ay, bx, by, cx, cy;
        switch (k) {
            case 0: {
                ax = v0.y;
                ay = v0.z;
                bx = v2.y - ax;
                by = v2.z - ay;
                cx = v1.y - ax;
                cy = v1.z - ay;
                break;
            }
            case 1: {
                ax = v0.z;
                ay = v0.x;
                bx = v2.z - ax;
                by = v2.x - ay;
                cx = v1.z - ax;
                cy = v1.x - ay;
                break;
            }
            case 2:
            default: {
                ax = v0.x;
                ay = v0.y;
                bx = v2.x - ax;
                by = v2.y - ay;
                cx = v1.x - ax;
                cy = v1.y - ay;
            }
        }
        float det = bx * cy - by * cx;
        bnu = -by / det;
        bnv = bx / det;
        bnd = (by * ax - bx * ay) / det;
        cnu = cy / det;
        cnv = -cx / det;
        cnd = (cx * ay - cy * ax) / det;
        basis = OrthoNormalBasis.makeFromWV(normal, Point3.sub(c, a, new Vector3()));
    }

    public void prepareShadingState(ShadingState state) {
        state.init();
        state.getRay().getPoint(state.getPoint());
        state.getNormal().set(normal);
        state.getGeoNormal().set(normal);
        state.setShader(shader);
        Point3 p = state.getPoint();
        float hu, hv;
        switch (k) {
            case 0: {
                hu = p.y;
                hv = p.z;
                break;
            }
            case 1: {
                hu = p.z;
                hv = p.x;
                break;
            }
            case 2: {
                hu = p.x;
                hv = p.y;
                break;
            }
            default:
                hu = hv = 0;
        }
        state.getUV().x = hu * bnu + hv * bnv + bnd;
        state.getUV().y = hu * cnu + hv * cnv + cnd;
        state.setBasis(basis);
    }

    public void intersect(Ray r, IntersectionState state) {
        float dn = normal.x * r.dx + normal.y * r.dy + normal.z * r.dz;
        if (dn == 0.0)
            return;
        float t = (((center.x - r.ox) * normal.x) + ((center.y - r.oy) * normal.y) + ((center.z - r.oz) * normal.z)) / dn;
        if (r.isInside(t)) {
            r.setMax(t);
            state.setIntersection(this, 0, 0, 0);
        }
    }
}